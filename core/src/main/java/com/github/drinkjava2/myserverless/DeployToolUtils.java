/* Copyright 2018-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at http://www.apache.org/licenses/LICENSE-2.0 Unless required by
 * applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS
 * OF ANY KIND, either express or implied. See the License for the specific
 * language governing permissions and limitations under the License.
 */
package com.github.drinkjava2.myserverless;

import static com.github.drinkjava2.myserverless.util.MyServerlessStrUtils.isEmpty;
import static com.github.drinkjava2.myserverless.util.MyServerlessStrUtils.positionOfPureChar;
import static com.github.drinkjava2.myserverless.util.MyServerlessStrUtils.substringAfter;
import static com.github.drinkjava2.myserverless.util.MyServerlessStrUtils.substringBefore;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.github.drinkjava2.myserverless.util.MyServerlessFileUtils;
import com.github.drinkjava2.myserverless.util.MyServerlessStrUtils;
import com.github.drinkjava2.myserverless.util.Systemout; 

/**
 * Store static methods for DeployTool
 * 
 * @author Yong Zhu
 * @since 1.0.0
 */
@SuppressWarnings("all")
public class DeployToolUtils {

	/**
	 * Extract sql/java to server side for one html/javascript file, if forceDeploy
	 * is true, ignore if have FRONT control word, force extract to server side
	 */
	public static void oneFileToServ(File file, boolean forceDeploy) {
		String text = MyServerlessFileUtils.readFile(file.getAbsolutePath(), "UTF-8");
		Map<String, SqlJavaPiece> formatedMap = new LinkedHashMap<String, SqlJavaPiece>();
		boolean changed = false;
		String formated = text;

		for (Entry<String, Class<?>> entry : MyServerlessEnv.getGsgtemplates().entrySet()) {
			String gsgMethod = entry.getKey();
			String gsgMtd_ = "$" + gsgMethod + "(`";
			Class<?> templateClass = entry.getValue();
			formatedMap.clear();
			formated = formatText(file, formated, formatedMap, gsgMtd_, '`');

            for (Entry<String, SqlJavaPiece> item : formatedMap.entrySet()) {
                changed = true;
                String key = item.getKey();
                SqlJavaPiece piece = item.getValue();
                String className = piece.getClassName();
                String src = SrcBuilder.createSourceCode(templateClass, PieceType.byGsgMethod(gsgMethod), piece);
                MyServerlessFileUtils.writeFile(MyServerlessEnv.getSrcDeployFolder() + "/" + className + ".java", src, "UTF-8");
                formated = MyServerlessStrUtils.replaceFirst(formated, key, "$gsg('" + className + "'");
            }
		}

		if (changed) {
			MyServerlessFileUtils.writeFile(file.getAbsolutePath(), formated, "UTF-8");
			Systemout.println("goServ => " + file.getAbsolutePath());
		}
	}

	/**
	 * Format text to detailed SqlJavaPiece map, return formatted string with key place holder
	 */
	private static String formatText(File file, String oldText, Map<String, SqlJavaPiece> map, String start,
			char endChar) {
		String result = oldText;
		boolean needTransfer = result.contains(start);
		while (needTransfer) {
			String front = substringBefore(result, start);
			String left = substringAfter(result, start);
			if (isEmpty(left))
				throw new IllegalArgumentException(
						"Error: " + start + " not closed ` in file: " + file.getAbsolutePath());

			int pos = positionOfPureChar(left, endChar);
			if (pos == -1)
				throw new IllegalArgumentException(
						"Error: " + start + " not found end ` in file: " + file.getAbsolutePath());

			String piece = left.substring(0, pos);
			String leftover = left.substring(pos + 1);
			SqlJavaPiece parsed = SqlJavaPiece.parseFromFrontText(start, piece);
			String key = "key" + MyServerlessStrUtils.getRandomString(20);
			map.put(key, parsed);
			result = front + key + leftover;
			needTransfer = result.contains(start);
		}
		return result;
	}

	/**
	 * Push back sql/java source code  from server side to HTML/HTM/JSP
	 */
	public static void oneFileToFront(File file, boolean forceGoFront, List<String> toDeleteJavas, boolean force) {
		String text = MyServerlessFileUtils.readFile(file.getAbsolutePath(), "UTF-8");
		if (!text.contains("$gsg('"))
			return;

		boolean changed = false;
		Map<String, SqlJavaPiece> map = new LinkedHashMap<String, SqlJavaPiece>();
		String formated = formatText(file, text, map, "$gsg('", '\'');
        for (Entry<String, SqlJavaPiece> item : map.entrySet()) {
            changed = true;
            String key = item.getKey();
            SqlJavaPiece piece = item.getValue();
            String javaSrcFileName;
            javaSrcFileName = MyServerlessEnv.getSrcDeployFolder() + "/" + piece.getOriginText() + ".java";
            String src = MyServerlessFileUtils.readFile(javaSrcFileName, "UTF-8");
            String template = MyServerlessStrUtils.substringBetween(src, "extends", "Template");
            template = MyServerlessStrUtils.substringAfterLast(template, ".");
            String gsgMethod = MyServerlessStrUtils.toLowerCaseFirstOne(template);
            SqlJavaPiece newPiece = SqlJavaPiece.parseFromJavaSrcFile(javaSrcFileName);
            if (MyServerlessStrUtils.isEmpty(piece.getOriginText()))
                formated = MyServerlessStrUtils.replaceFirst(formated, key, "$gsg('" + piece.getOriginText() + "'");
            else {
                String classId=MyServerlessStrUtils.substringBefore(piece.getOriginText(), "_"); //to get #classId
                if("default".equalsIgnoreCase(classId))
                    classId="";
                else 
                    classId="#"+classId+" ";
                String newPieceStr = SrcBuilder.createFrontText(PieceType.byGsgMethod(gsgMethod), newPiece);
                formated = MyServerlessStrUtils.replaceFirst(formated, key, "$" + gsgMethod + "(`"+ classId + newPieceStr + "`");
                toDeleteJavas.add(javaSrcFileName);
            }

        }
		map.clear();
		if (changed) {
			MyServerlessFileUtils.writeFile(file.getAbsolutePath(), formated, "UTF-8");
			Systemout.println("goFront => " + file.getAbsolutePath());
		}
	}

}
