### MyServerless(原GoSqlGo)简介 | Description
天下武功，唯快不破，MyServerless能让前端直接在HTML或Javascript里写业务代码和SQL，不需要后端程序员参与，从而实现快速开发。 

MyServerless项目原名为GoSqlGo，因为考虑到它的开发模式有点类似于serverless，后端仅负责编译和执行前端脚本，不参与具体业务开发，而且都是以远程方法作为业务最小功能单元，所以从3.0版起项目更名为MyServerless。

MyServerless与通常的Serverless服务相比，主要区别是：  
1.使用免费。通常大厂的Serverless服务是按调用来收费，而MyServerless是用户自己布署的，调用时不收费。  
2.可定制，无依赖。开发者拥有全部的MyServerless服务端源码，可以自行定制后端服务，如选择不同的签权方式、DAO工具等，不与具体的云服务商绑定，可随时布署到不同的云服务或自有服务器。
3.技术极简。大厂的Serverless服务通常很复杂而且每家都不一样，很难上手。而MyServerless核心源码只有几千行，采用标准Java脚本语言和SQL，极易上手，通常前端只需要了学会SQL即可上手进行开发。  
4.功能极度简化。MyServerless的定位是用最少的代价给前端开通数据库访问能力。相比与大厂的Serverless服务，它在功能上有缺失：  
  1)目前不提供在线IDE编辑器。  
  2)目前后端服务不提供高可用性、后端自动缩扩容这些功能。  
  3)目前只专注于数据库访问，不支持一些特殊功能如文件上传等。  
  4)大厂Serverless服务通常不区分开发期和布署期，而MyServerless因为安全原因，分为开发和布署两个阶段，开发期源码写在前端，布署期要利用打包工具将前端的源码和SQL抽取到后端。

### 适用场合 | Applications
MyServerless适用于原型、快速、简单业务开发，以及前后端都是一个人开发的场合。  

### 不适用场合 | Not Applicable
MyServerless不适用于复杂业务开发(比如脚本源码超过50行)，原因是因为目前不具备IDE插件可以调试存放在前端的Java脚本，调试效率低，如果业务复杂时，在调试上花的时间还不如直接让传统后端程序员提供API和文档。  

通常网站的API是由占绝大多数的简单CRUD和少部分的复杂业务API组成，大项目也可以考虑用MyServerless结合传统API方式来开发，开启一个MyServerless服务支撑简单的CRUD功能，剩下的复杂业务依然由传统后端程序员提供API和文档，这样可以节省后端程序员大部分的开发工作量。  

### 使用 | Usage
用实例来说明MyServerless的使用，以下示例直接在前端写SQL和Java脚本，实测通过，文件位于[这里](https://gitee.com/drinkjava2/myserverless/blob/master/server/src/main/webapp/page/demo1.html)。  
```
<!DOCTYPE html>
<html>
 <head>
 <script src="/js/jquery-1.11.3.min.js"></script>
 <script src="/js/jquery-ajax-ext.js"></script>
 <script src="/js/myserverless-3.0.js"></script>
 </head>
 <body>
      
    <script>document.write($java(`return new WebBox("/WEB-INF/menu.html");`)); </script>
    <h2>Transaction demo, use jQuery</h2>
    
    <script> 
	  function getUserListHtml(){
		  var users=$qryMapList(`select * from account where amount>=? order by id`,0);
		  var html="User List:<br/>";
		  for(var i=0;i<users.length;i++) 
			  html+="User ID:" +  users[i].ID+", AMOUNT:"+ users[i].AMOUNT+"<br/>"; 
	      return html;		   
	  }
	</script>
	   
	<div id="msgid" class="msg"></div> 
	
	<section>
		<header>Account A</header>
		<div id="A" class="amount">
			<script>
				document.write($qryObject(`select amount from account where id=? and amount>=?`, 'A',0));
			</script>
		</div>
	</section>
	<section>
		<header>Account B</header>
		<div id="B" class="amount">
			<script>
			    account=$qryEntity(`com.demo.entity.Account, select * from account where id=?`, 'B');
				document.write(account.amount);
			</script>
		</div>
	</section>
	<script>
	  function transfer(from, to, money){ 
			var rst = $$javaTx(`#userTransfer
					int money = Integer.parseInt($3);
					if (money <= 0)
					     return new JsonResult(0, "Error: Illegal input.");
					Account a = new Account().setId($1).load();
					if (a.getAmount() < money)
					     return new JsonResult(0, "Error:No enough balance!");
					Account b = new Account().setId($2).load();//加载账户
					a.setAmount(a.getAmount() - money).update();
					b.setAmount(b.getAmount() + money).update();
					    return new JsonResult(200, "Transfer success!").setDataArray(a.getAmount(), b.getAmount());
			        `, from,to,money); 
		  $("#msgid").text(rst.msg);	
		  if(rst.code==200) { 
	 	      $("#"+from).text(rst.data[0]);
	 	      $("#"+to).text(rst.data[1]);
	 	      $("#msgid").css("background", "#dfb");
		  }
		  else $("#msgid").css("background", "#ffbeb8");		  
		}
	</script>
	<section>
		<header>Transfer</header>
		<form onsubmit="return false" action="##" method="post">
			<input id="amount" name="amount" value="100" class="amount">
			<button name="btnA2B" value="true" onclick="transfer('A','B', $('#amount').val())">From account A to account B</button>
			<button name="btnB2A" value="true" onclick="transfer('B','A',$('#amount').val())">From account B to account A</button>
		</form>
	</section>
 </body>
</html>
```
另外还有两个演示:  
[演示2](https://gitee.com/drinkjava2/myserverless/blob/master/server/src/main/webapp/page/demo2.html)：MyServerless结合Vue的使用。  
[演示3](https://gitee.com/drinkjava2/myserverless/blob/master/server/src/main/webapp/page/demo3.html): 演示直接在前端进行表单的输入检查并保存到数据库  

### 运行 | Dependency and Run
MyServerless分为server和core两个目录，server目录是一个示范项目，使用时只需要将server项目作一些修改，如更改数据库连接和重写签权逻辑，即可以用于实际开发 。core目录是内核源码，除非要定制后端，用户一般不需要关心。  
在windows下点击server目录下的run_server.bat批处理，即可进入http://localhost演示界面， 使用用户名demo、密码123登录。  

### 方法说明 | Methods
在前端引入myserverless-3.0.js这个javascript库后，就可以直接在前端调用以下远程函数执行后端业务：
```
$java(String, Object...) 在后端执行Java脚本，第一个参数是Java脚本源码，后续参数是业务参数，在Java脚本源码里可以用$1,$2...来代表。  
$javaTx(String, Object...) 在后端执行Java脚本并开启事务，如果有异常发生，事务回滚。  
$qryObject(String, Object...) 将SQL查询结果的第一行第一列值返回，第一个参数是SQL，后面是SQL参数，下同  
$qryArray(String, Object...)  返回SQL查询的第一行数据，以Javascript数组对象格式返回  
$qryArrayList(String, Object...)  返回多行查询结果，以数组列表格式返回    
$qryTitleArrayList(String, Object...)  返回多行查询结果，以数组列表格式返回,第一行内容是各个列的标题  
$qryMap(String, Object...) 返回SQL查询的第一行数据，为Map 格式  
$qryMapList(String, Object...)  返回SQL查询的多行数据，为List<Map>格式  
$qryEntity(String, Object...)  返回第一行数据为实体对象，SQL写法是实体类名+逗号+SQL, 示例:$qryEntity(`a.b.Demo, select * from demo`); 
$qryEntityList(String, Object...)  返回多行数据为List<实体>对象，SQL写法是实体类名+逗号+SQL, 示例:$qryEntityList(`a.b.Demo, select * from demo`);   
```
注意以上远程函数调用的第一个参数是Java源码或SQL文本，要用键盘ESC下方的单引号括起来，这是Javascript的特殊单引号，支持多行文本。    
以上方法都是自定义的，用户也可以自定义自己的方法。以上方法还可以用$$开头返回JSON对象。JSON对象有{code, msg, data, debugInfo} 4个字段，但debugInfo字段仅当服务端配置为debug_info=true时才有值。  
MyServerless方法可以添加以下两类特殊语句：
 1. #xxxxx 形式的ID，用来自定义方法ID，如没有这个ID，方法缺省ID为"Default"。在用户的签权类里，要根据这个方法ID来判断用户是否有权限执行这个方法
 2. import开头的语句，这个等同于标准的Java包引入语法  
示例下面这个方法调用定义了一个名为ReadUserAmount的方法ID，并引入了一个名为abc.DemoUser的Java包:   
$java('#ReadUserAmount import abc.DemoUser; return new DemoUser().loadById($1).getAmount();', 'u1');   

 
### 开发和布署 | Develop & Deploy
在类根目录(项目的resources目录)下，有一个名为myserverless.properties的配置文件，可以进行配置，例如配置deploy目录、设定开发/生产阶段、设定develop_token和debug_inifo等，详见它的注释。  

开发阶段：MyServerless示范项目在服务端运行，它自带一个动态Java脚本编译功能，前端发来的SQL和Java脚本，被动态编译为实际的Java类，并执行这个Java类，最后返回JSON对象。  
                  如果javascript方法前是两个$符号，如$$java，则返回一个JSON对象，它的data字段保存了返回结果。  
                  如果javascript方法前只有一个$符号，如$java，则返回的值直接就是Json的data字段。  

布署阶段：双击server目录下的批处理文件go-server.bat，即可将前端所有的SQL和原生Java片段抽取到服务端去，转变为Java源文件，原有前端的SQl和JAVA代码在转换后将成为类似于$gsg('Xxxx_C9GK90J27','A');之类的通过ID进行的调用，以实现安全性。  
server目录下还有一个文件名为go-front.bat，这个是逆操作，可以将后端的Java代码移回到前端。  

### 安全 | Security
在项目的myserverless.properties文件里，有以下关于安全的设计：  
1.当stage设定为product阶段时，不再动态编译前端传来的SQL和Java片段，以实现运行期的安全。  
2.当stage设定为develop时，deveop_token必须设定一个密码，在开发阶段会 执行前端传来的任意SQL和Java代码，后端会检查前端传来的develop_token密码与服务器的设定是否一致，否则拒绝访问，这样可以排除开发期的非法访问。  
3.token_security设定，这是用来登记用户自定义的登录检查类，使用详见示范项目，MyServerless并没有集成第三方权限框架，所以需要用户根据自已的项目实现自定义的登录和token验证类。通常是根据token和方法ID来判定是否允许执行MyServerless方法。

### 常见问题 | FAQ
* 安全上有没有问题?  
架构上没有安全问题，MyServerless通过token和方法ID，结合自定义签权方法来检查每一个方法ID的执行权限。当然用户写的签权方法或Java脚本中有可能出现安全漏洞，但这与本项目的架构无关。  

* 为什么采用Java作为业务脚本语言而不是Javascript/C#/Go等语言? 
因为作者只对Java熟悉，没有精力象大厂的Serverless服务一样提供多种语言实现。其实用户如果对其它语言熟悉，也可以仿照MyServerless的思路编写自己的serverless服务，原理不复杂，无非就是源码保存在远程动态编译执行，布署时再抽取出来以实现安全性。

* 为什么默认server项目采用jSqlBox这么小众的DAO工具?  
因为jSqlBox是本人写的DAO工具，打广告用的，它的SQL写法很多。如果前端对jSqlBox不感冒，可以仿照示例改造成使用不同的DAO工具如MyBatis等。MyServerless重点在于提供了一个动态编译执行远程Java源码的框架，不拘泥于具体的某个技术。  

* (小鹏提出)Java写在HTML/Javascript里没有错误检查、语法提示，及重构功能，不利于复杂业务开发。  
这个将来可以通过开发IDE插件解决。但目前的解决办法是只能运行go-server.bat批处理将Sql/Java抽取成Java源码类，在Eclipse/Idea等IDE里找错、更正后再用go-front.bat批处理塞回到HTML里去，也可以干脆就不塞回去了，后者就对应传统的前后端分离开发情形。  

* 业务有变动，前端代码或SQL需要修改怎么办?  
开发期直接在前端修改Java代码或SQL即可，即改即生效，不需要重启后端服务器。布署时由运维布署并重启产品服务器。  

* 前端业务代码需要复用(如多处调用或测试)怎么办?  
需要复用的业务代码和SQL写在公共JavaScript库里，前端其它地方调用这些公共库里的方法。  

* 与GraphQL或XXX-API等项目的区别？
以上项目是基于API及文档的创建和使用，而MyServerless是直接在前端写Java脚本和SQL，根本就不创建API。
 
## 相关开源项目 | Related Projects
- [ORM数据库工具 jSqlBox](https://gitee.com/drinkjava2/jSqlBox)  

## 期望 | Futures
如对MyServerless感兴趣请点个赞，或发issue提出完善意见

## 版权 | License
[Apache 2.0](http://www.apache.org/licenses/LICENSE-2.0)

## 关注我 | About Me
[Github](https://github.com/drinkjava2)  
[码云](https://gitee.com/drinkjava2)   
