### MyServerless简介 | Description
天下武功，唯快不破，网站开发无非就是接收用户输入、存到数据库。MyServerless能让前端直接写业务逻辑和存取数据库，独立完成项目开发。

MyServerless是一个运行于后端的服务。它的主要特点是分为两个阶段：开发期把SQL和Java脚本写在前端HTML或Javascript中进行远程开发，布署期由管理员利用打包工具将SQL和Java从前端移到后端进行布署，以达到隐藏源码实现安全性的目的。  

MyServerless项目原名为GoSqlGo，意思是可以直接在前端写SQL的意思，但因为考虑到它的开发模式实际上和大厂的Serverless模式有点类似，所以这个项目从3.0版开始更名为MyServerless。  

MyServerless的主要优点是开发效率高，因为它是一种无API开发模式，前端可以直接远程在HTML和Javascript里写业务源代码和SQL进行开发，不需要后端程序员的参与。  

MyServerless与通常大厂提供的Serverless服务相比，主要区别是：  
1.免费。大厂的Serverless服务是按调用来收费，而MyServerless是用户自己布署的，不需要付费。  
2.可定制。开发者可以自行修改后端服务，如选择不同的签权工具、DAO工具等。
3.上手容易。大厂的Serverless服务通常非常复杂，很难学习和使用，而MyServerless采用Java脚本和SQL，所有文档就只有一个Readme，通常前端花一天时间学习SQL即可上手进行开发。  
4.功能极度简化。MyServerless是个简化版的Serverless工具，相比与大厂的Serverless服务，在以下功能上有缺失：  
  1)目前不提供在线IDE编辑器。  
  2)目前不具备高可用性、高流量自动扩容这些高端功能。  
  3)只专注于数据库存取，不支持一些特殊功能如文件上传等。  

### 适用场合 | Applications
MyServerless是一个独立的服务，通常使用token进行签权，可以使用在新项目开发上，也可以与任意旧的后端项目混搭使用。  

### 不适用场合 | Not Applicable
MyServerless不适用于复杂业务开发(比如脚本源码超过50行)，原因是因为目前不提供在线IDE编辑器，调试效率低，如果业务复杂时，在调试上花的时间还不如让后端程序员提供API来的快。  
通常网站的API是由80%的简单CRUD调用+20%的复杂业务API组成，所以对于大项目，可以考虑用MyServerless+传统API方式结合的模式来开发，开启一个MyServerless服务提供CRUD功能，剩下的20%的复杂业务依然由传统后端程序员提供API，这样可以节省80%的后端工作量。  

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
MyServerless项目分为两个目录，core目录为MyServerless内核源码，普通用户一般不需要关心。在windows下点击server目录下的\run_server.bat批处理，并用用户名demo、密码123登录即可进入演示。  
server目录是一个示范项目，使用时只需要将server示范项目作一些修改即可以用于实际开发，如更改数据库连接和重写签权逻辑。

### 方法 | Methods
示范项目里主要有以下方法：
```
$java(String, Object...) 执行多行服务端Java语句。第一个参数是Java本体，后面是参数，在Java里可以用$1,$2...来访问。  
$javaTx(String, Object...) 执行多行服务端Java语句并开启事务，如果有异常发生，事务回滚。  
$qryObject(String, Object...) 将SQL查询结果的第一行第一列对象返回，第一个参数是SQL，后面是SQL参数，下同  
$qryArray(String, Object...)  返回SQL查询的第一行数据  
$qryArrayList(String, Object...)  返回多行查询结果    
$qryTitleArrayList(String, Object...)  返回多行查询结果，第一行内容是各个列的标题  
$qryMap(String, Object...) 返回SQL查询的第一行数据，为Map 格式  
$qryMapList(String, Object...)  返回SQL查询的多行数据，为List<Map>格式  
$qryEntity(String, Object...)  返回第一行数据为实体对象，SQL写法是实体类名+逗号+SQL, 示例:$qryEntity(`a.b.Demo, select * from demo`); 
$qryEntityList(String, Object...)  返回多行数据为List<实体>对象，SQL写法是实体类名+逗号+SQL, 示例:$qryEntityList(`a.b.Demo, select * from demo`);   
```
注意以上方法都是自定义的，用户也可以自定义自己的方法。以上方法还可以用$$开头返回JSON对象。JSON对象有{code, msg, data, debugInfo} 4个字段，但debugInfo字段仅当服务端配置为debug_info=true时才有值。  
MyServerless方法可以添加以下两类特殊语句：
 1. #xxxxx 形式的ID，用来自定义方法ID，如没有这个ID，方法缺省ID为"Default"
 2. import开头的Java包引入语句。  
示例：   
$java('#ReadUserAmount import abc.DemoUser; return new DemoUser().loadById($1).getAmount();', 'u1');   

在类根目录(项目的resources目录)下，有一个名为myserverless.properties的配置文件，可以进行配置，例如配置deploy目录、设定开发/生产阶段、设定develop_token和debug_inifo等，详见它的注释。  
注意以上方法必须用键盘ESC下方的单引号括起来，这是Javascript的特殊单引号，支持多行文本，不能用普通的单引号。  

开发阶段：MyServerless示范项目在服务端运行，它自带一个动态编译工具，前端发来的SQL和Java片段，被动态编译为Java类，并调用服务端DAO工具，最后返回JSON对象。  
MyServerless如果方法前是两个$符号，如$$java，则返回一个JSON对象，它的data字段保存了返回结果。如果方法前只有一个$符号，如$java，返回的值直接就是javascript对象了，也就是Json的data字段。  
 
### 布署 | Deploy
布署阶段：双击server目录下的批处理文件go-server.bat，即可将前端所有的SQL和原生Java片段抽取到服务端去，转变为Java源文件，原有前端的SQl和JAVA代码在转换后将成为类似于$gsg('Xxxx_C9GK90J27','A');之类的通过ID进行的调用，以实现安全性。  
server目录下还有一个文件名为go-Front.bat，这个是逆操作，可以将后端的Java代码移回到前端。  

### 安全 | Security
在项目的myserverless.properties文件里，有以下关于安全的设定：  
1.stage设定，可以设定为develop或product，当设定为product阶段时，不再接收前端传来的SQL和Java片段  
2.deveop_token设定，这个设定仅在develop阶段生效，开发阶段前端传来的develop_token必须与服务器的设定一致，否则拒绝访问，这样可以排除非法访问。  
3.token_security设定，这是用来登记用户自定义的登录检查和token验证逻辑类，使用详见示范项目，MyServerless并没有集成第三方权限框架，所以需要用户根据自已的项目实现自定义的登录和token验证类。通常是根据token和方法ID来判定是否允许执行MyServerless方法。

### 常见问题 | FAQ
* 安全上有没有问题?  
架构上没有安全问题，MyServerless通过token和方法ID，结合自定义签权方法来检查每一个ID对应方法的执行权限。当然用户自已写的签权模块或Java脚本中有可能出现安全漏洞，但这个与MyServerless的架构无关了。  

* 为什么采用Java作为前端脚本语言而不是Javascript或Go语言?
因为Java是流行的后端语言，生态较全，MyServerless采用Java语言作为业务脚本语言，可以为以后功能扩充作准备，如集群、分库分表、自动扩容等。  

* 为什么示例项目采用jSqlBox这么小众的DAO工具?  
因为jSqlBox是本人写的DAO工具，打广告用的，它的DAO功能很全。如果前端对jSqlBox不感冒，可以仿照示例改造成使用不同的DAO工具如JDBC-Template、MyBatis等。  

* (小鹏提出)Java写在HTML/Javascript里没有错误检查、语法提示，及重构功能，不利于复杂业务开发。  
这个将来可以通过提供在线IDE解决。但目前的解决办法是只能求助于传统后端IDE，运行go-server.bat批处理将Sql/Java抽取成Java源码类，在Eclipse/Idea等IDE里找错、更正后再用go-front.bat批处理塞回到HTML里去，也可以干脆就不塞回去了，后者就对应传统的前后端分离开发情形。  

* 前端Java脚本或SQL有新版本怎么办?  
直接在前端源码里改动即可。MyServerless里没有版本号这种说法，因为它是没有API的，业务都是写在前端，所有前端脚本或SQL改动立即生效，不需要重启后端服务器。  

* 前端业务代码需要复用(多处调用)怎么办?  
需要复用的业务代码写在公共JavaScript库里，其它的Javascript可以调用这些方法。  
  
* MyServerless的业务代码怎么做单元测试和集成测试?  
需要测试的业务代码写在公共JavaScript库里，前端测试脚本可以调用这些方法。  


## 相关开源项目 | Related Projects
- [ORM数据库工具 jSqlBox](https://gitee.com/drinkjava2/jSqlBox)  

## 期望 | Futures
如对MyServerless感兴趣请点赞，或发issue提出完善意见，也欢迎提交演示示例。

## 版权 | License
[Apache 2.0](http://www.apache.org/licenses/LICENSE-2.0)

## 关注我 | About Me
[Github](https://github.com/drinkjava2)  
[码云](https://gitee.com/drinkjava2)   
