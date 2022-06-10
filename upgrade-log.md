

- 问题1、编译时报错：javax.activation不存在

   - 问题分析：需额外添加javax.mail依赖

   ```text
   <dependency>
      <groupId>javax.activation</groupId>
      <artifactId>activation</artifactId>
      <version>1.1.1</version>
   </dependency>
   ```

- 问题2、运行单元测试时报错：

```text
[INFO] -------------------------------------------------------
[INFO]  T E S T S
[INFO] -------------------------------------------------------
[INFO] Running io.github.jiashunx.masker.rest.jjwt.MRestJWTHelperTest
1654851338907
SLF4J: Failed to load class "org.slf4j.impl.StaticLoggerBinder".
SLF4J: Defaulting to no-operation (NOP) logger implementation
SLF4J: See http://www.slf4j.org/codes.html#StaticLoggerBinder for further details.
java.lang.NoClassDefFoundError: javax/xml/bind/DatatypeConverter
	at io.jsonwebtoken.impl.Base64Codec.decode(Base64Codec.java:26)
	at io.jsonwebtoken.impl.DefaultJwtParser.setSigningKey(DefaultJwtParser.java:151)
	at io.github.jiashunx.masker.rest.jjwt.MRestJWTHelper.isTokenValid(MRestJWTHelper.java:140)
	at io.github.jiashunx.masker.rest.jjwt.MRestJWTHelperTest.test(MRestJWTHelperTest.java:18)
	at java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
	at java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
	at java.base/jdk.internal.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
	at java.base/java.lang.reflect.Method.invoke(Method.java:566)
	at org.junit.runners.model.FrameworkMethod$1.runReflectiveCall(FrameworkMethod.java:59)
	at org.junit.internal.runners.model.ReflectiveCallable.run(ReflectiveCallable.java:12)
	at org.junit.runners.model.FrameworkMethod.invokeExplosively(FrameworkMethod.java:56)
	at org.junit.internal.runners.statements.InvokeMethod.evaluate(InvokeMethod.java:17)
	at org.junit.runners.ParentRunner$3.evaluate(ParentRunner.java:306)
	at org.junit.runners.BlockJUnit4ClassRunner$1.evaluate(BlockJUnit4ClassRunner.java:100)
	at org.junit.runners.ParentRunner.runLeaf(ParentRunner.java:366)
	at org.junit.runners.BlockJUnit4ClassRunner.runChild(BlockJUnit4ClassRunner.java:103)
	at org.junit.runners.BlockJUnit4ClassRunner.runChild(BlockJUnit4ClassRunner.java:63)
	at org.junit.runners.ParentRunner$4.run(ParentRunner.java:331)
	at org.junit.runners.ParentRunner$1.schedule(ParentRunner.java:79)
	at org.junit.runners.ParentRunner.runChildren(ParentRunner.java:329)
	at org.junit.runners.ParentRunner.access$100(ParentRunner.java:66)
	at org.junit.runners.ParentRunner$2.evaluate(ParentRunner.java:293)
	at org.junit.runners.ParentRunner$3.evaluate(ParentRunner.java:306)
	at org.junit.runners.ParentRunner.run(ParentRunner.java:413)
	at org.apache.maven.surefire.junit4.JUnit4Provider.execute(JUnit4Provider.java:365)
	at org.apache.maven.surefire.junit4.JUnit4Provider.executeWithRerun(JUnit4Provider.java:273)
	at org.apache.maven.surefire.junit4.JUnit4Provider.executeTestSet(JUnit4Provider.java:238)
	at org.apache.maven.surefire.junit4.JUnit4Provider.invoke(JUnit4Provider.java:159)
	at org.apache.maven.surefire.booter.ForkedBooter.invokeProviderInSameClassLoader(ForkedBooter.java:379)
	at org.apache.maven.surefire.booter.ForkedBooter.runSuitesInProcess(ForkedBooter.java:340)
	at org.apache.maven.surefire.booter.ForkedBooter.execute(ForkedBooter.java:125)
	at org.apache.maven.surefire.booter.ForkedBooter.main(ForkedBooter.java:413)
[ERROR] Tests run: 1, Failures: 1, Errors: 0, Skipped: 0, Time elapsed: 0.791 s <<< FAILURE! - in io.github.jiashunx.masker.rest.jjwt.MRestJWTHelperTest
[ERROR] test(io.github.jiashunx.masker.rest.jjwt.MRestJWTHelperTest)  Time elapsed: 0.584 s  <<< FAILURE!
java.lang.AssertionError
	at io.github.jiashunx.masker.rest.jjwt.MRestJWTHelperTest.test(MRestJWTHelperTest.java:18)
[INFO] 
[INFO] Results:
[INFO] 
[ERROR] Failures: 
[ERROR]   MRestJWTHelperTest.test:18
[INFO] 
[ERROR] Tests run: 1, Failures: 1, Errors: 0, Skipped: 0
[INFO]
```

   - 问题分析：JAXB API是java EE 的API，JDK11删除了这个工具，手工添加依赖即可

   ```text
   <dependency>
       <groupId>javax.xml.bind</groupId>
       <artifactId>jaxb-api</artifactId>
       <version>2.3.0</version>
   </dependency>
   <dependency>
       <groupId>com.sun.xml.bind</groupId>
       <artifactId>jaxb-impl</artifactId>
       <version>2.3.0</version>
   </dependency>
   <dependency>
       <groupId>com.sun.xml.bind</groupId>
       <artifactId>jaxb-core</artifactId>
       <version>2.3.0</version>
   </dependency>
   <dependency>
       <groupId>javax.activation</groupId>
       <artifactId>activation</artifactId>
       <version>1.1.1</version>
   </dependency>
   ```
