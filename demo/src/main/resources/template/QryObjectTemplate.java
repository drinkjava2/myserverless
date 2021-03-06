package template;

import static com.github.drinkjava2.jsqlbox.DB.par;
import static com.github.drinkjava2.jsqlbox.DB.qryObject;
import static com.github.drinkjava2.jsqlbox.DB.qry;
import com.github.drinkjava2.jsqlbox.*;
import com.github.drinkjava2.myserverless.BaseTemplate;

@SuppressWarnings("unused")
public class QryObjectTemplate extends BaseTemplate {

	@Override
	public Object executeBody() {
		/* MYSERVERLESS BODY BEGIN */
		String sql = null;
		/* MYSERVERLESS BODY END */
		Object[] paramArray = getParamArray();
		if (paramArray.length == 0)
			return qryObject(sql);
		else
			return qryObject(sql, par((Object[])paramArray));
	}

}
