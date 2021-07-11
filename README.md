### 注：项目命名从GoSqlGo改名为MyServerless，正在整理代码，待发布

### MyServerless(原名GoSqlGo)简介 | Description
天下武功，唯快不破，程序无非就是接收用户输入、存到数据库。MyServerless能让前端直接写业务逻辑和存取数据库，独立完成项目开发。

MyServerless是一个运行于后端的服务。它的主要特点是分为两个阶段：开发期把SQL和Java脚本写在前端HTML或Javascript中进行远程开发，布署期由管理员利用打包工具将SQL和Java从前端移到后端进行布署，以达到隐藏源码实现安全性的目的。  

MyServerless项目原名为GoSqlGo，意思是可以直接在前端写SQL的意思，但因为考虑到它的开发模式实际上和大厂的Serverless方式有点类似，所以这个项目从3.0版开始更名为MyServerless。  

MyServerless的主要优点在于它是一种无API开发模式，前端可以直接远程在HTML和Javascript里写业务代码和SQL进行开发，不需要后端程序员提供API，所以开发效率高。简单来说就是无招胜有招。  

MyServerless与通常大厂提供的Serverless服务相比，主要区别是：  
1.免费。因为MyServerless是用户自己编写和布署的，所以不需要付费。  
2.高度可定制。用户可以自行DIY后端逻辑和工具，如签权、DAO工具等。
3.上手容易。大厂的Serverless服务非常复杂，很难学习和使用，而MyServerless采用Java脚本和SQL，通常前端一天时间学习即可上手进行开发。  
4.功能有缺失。MyServerless是个极简版的Serverless工具，以下功能缺失：  
  a.目前版本不提供在线IDE编辑器。  
  b.目前版本不提供后端高可用性、高可靠性、自动扩容这些高端功能。  
  c.目前版本不提供文件上传功能。    

### 适用场合 | Applications
适用于快速、原型开发、业务逻辑简单的场合。MyServerless是独立的服务，通常使用token进行签权，所以也可以与旧项目混搭使用，而不需要更改旧项目的后端源码。多个MyServerless服务也可以集群使用，以提高性能。  

### 不适用场合 | Not Applicable
MyServerless目前不适用于复杂的业务开发(比如源码超过50行)，原因不是架构和安全问题，而是因为当前版本不提供在线IDE编辑器，脚本的开发调试效率低，省略掉API的时间会被没有IDE这个缺点抵消。  
但因为通常项目组成是由80%的简单CRUD调用+20%的复杂业务调用组成，所以对于大项目，也可以考虑用MyServerless+传统API方式结合的方式来进行开发，由MyServerless完成80%的CRUD操作，剩下的20%的复杂业务API依然由传统后端程序员提供。  

### 使用 | Usage
用例子来说明MyServerless的使用，以下示例直接在前端写SQL和Java脚本，实测通过，文件位于[这里](https://gitee.com/drinkjava2/myserverless/blob/master/server/src/main/webapp/page/demo1.html)。  
```
<!DOCTYPE html>
<html>
 <head>
 <script src="/js/jquery-1.11.3.min.js"></script>
 <script src="/js/jquery-ajax-ext.js"></script>
 <script src="/js/myserverless3.0.js"></script>
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
			var rst = $$javaTx(`
					int money = Integer.parseInt($3);
					if (money <= 0)
					     return new JsonResult(0, "Error: Illegal input.");
					Account a = new Account().setId($1).load();
					if (a.getAmount() < money)
					     return new JsonResult(0, "Error:No enough balance!");
					Account b = new Account().setId($2).load();
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
MyServerless项目分为两个目录，core目录为MyServerless内核包源码，普通用户不需要关心内核包。server目录是一个示范项目，使用时用户只需要将server示范项目作一些修改即可以用于实际开发，如更改数据库连接和重写签权类代码。  
在windows下点击server目录下的\run_server.bat批处理，并用用户名demo、密码123登录即可进入演示。  

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
$java('#ReadAmount import abc.DemoUser; return new DemoUser().loadById($1).getAmount();', 'u1');   

在类根目录(项目的resources目录)下，有一个名为MyServerless.properties的配置文件，可以进行一些配置，例如配置deploy目录、设定开发/生产阶段、设定develop_token和debug_inifo等
注意以上方法必须用键盘ESC下方的单引号括起来，这是Javascript的特殊单引号，支持多行文本，不能用普通的单引号。 

开发阶段：MyServerless示范项目在服务端运行，它自带一个动态编译工具，前端发来的SQL和Java片段，被动态编译为Java类，并调用服务端DAO工具，最后返回JSON对象。  
MyServerless如果方法前是两个$符号，如$$java，则返回一个JSON对象，它的data字段保存了返回结果。如果方法前只有一个$符号，如$java，返回的值直接就是javascript对象了，也就是Json的data字段。
 
### 布署 | Deploy
布署阶段：双击运行打包工具goServer.bat，将前端所有的SQL和原生Java片段抽取到服务端去，转变为可调试的Java源文件，原有前端的SQl和JAVA代码在转换后将成为类似于$gsg('Xxxx_C9GK90J27','A');之类的通过ID进行的调用，以实现安全性。 server目录下还有一个文件名为goFront.bat，这个是逆操作，可以将后端的Java代码移回到前端。

### 安全 | Security
在示范项目的myserverless.properties文件里，有以下关于安全的设定：  
1.stage设定，可以设定为develop或product，当设定为product阶段时，不再接收前端传来的SQL和Java片段  
2.deveop_token设定，这个设定仅在develop阶段生效，开发阶段前端传来的develop_token必须与服务器的设定一致，否则拒绝访问，这样可以排除非法访问。  
3.token_security设定，这是用来登记用户自定义的登录检查和token验证逻辑类，使用详见示范项目，MyServerless并没有集成第三方权限框架，所以需要用户根据自已的项目实现自定义的登录和token验证类。通常是根据token和方法ID来判定是否允许执行MyServerless方法。

### 常见问题 | FAQ
* 安全上有没有问题?  
架构上没有安全问题，MyServerless通过token和方法ID，结合自定义签权方法来检查每一个MyServerless调用是否有执行权限。当然自定义签权方法或java脚本有可能出现安全漏洞，但这个与MyServerless无关了。  

* 为什么采用Java作为脚本语言而不是Javascript或Go?
因为Java是主流的后端语言，后端生态较全，MyServerless采用Java作为脚本语言，可以方便今后添加功能扩充。  

* 为什么示例项目采用jSqlBox这么小众的DAO工具?  
因为jSqlBox是本人写的DAO工具，打广告用的，它的DAO功能很全。如果前端对jSqlBox不感冒，可以仿照示例改用不同的DAO工具如MyBatis等。  

* (小鹏提出)Java写在HTML/Javascript里没有错误检查、语法提示，及重构功能。  
这个将来可以通过开发在线IDE解决，目前的一个解决办法是只能运行goServ.bat批处理将Sql/Java抽取成Java源码类，在IDE里找错、更正后再用goFront.bat批处理塞回到HTML里去，也可以干脆就不塞回去了，后者就对应传统的前后端分离开发情形。  

* 前端Java脚本或SQL有新版本怎么办?  
  直接在前端源码里改动即可。MyServerless里没有版本号这种说法，因为它是没有API的，所有前端Java脚本源码或SQL改动立即生效，也不需要重启后端服务器。  

* 前端业务代码需要复用(多处调用)怎么办?  
  需要复用的业务代码写在公共JavaScript库里，其它的Javascript可以调用这些方法。  
  
* 怎么做单元测试和集成测试?  
  需要测试的业务代码写在公共JavaScript库里，前端测试脚本可以调用这些方法。  

## 相关开源项目 | Related Projects
- [ORM数据库工具 jSqlBox](https://gitee.com/drinkjava2/jSqlBox)  

## 期望 | Futures
MyServerless已发布，对它感兴趣的请加关注，或发issue提出完善意见。也欢迎同学们提交MyServerless演示示例。

## 版权 | License
[Apache 2.0](http://www.apache.org/licenses/LICENSE-2.0)

## 关注我 | About Me
[Github](https://github.com/drinkjava2)  
[码云](https://gitee.com/drinkjava2)   
