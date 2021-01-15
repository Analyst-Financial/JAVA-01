## 学习笔记

### 1.Class文件结构

&emsp;Class文件是以8个字节为基础单位的二进制流，且各个数据严格按照顺序紧凑的排列在文件之中，中间没有任何的分隔符。并且按照大端格式进行排列（x86使用小端格式来进行存储数据）。

&emsp;&emsp;Class类文件共有两种格式：无符号数，表。

- 无符号数：基本的数据类型，具有u1,u2,u4,u8代表1个字节，2个字节，4个字节，8个字节.可以描述数字，索引引用，字符串值。

- 表：由多个无符号数或其他表组成的复合数据类型。习惯以_info结尾命名。

- 为了方便描述同一类型但数量不定的多个数据，用一个计数器加若干个数据的形式来描述，称为集合。

**表1 Class文件格式**

|      类型      |        名称         |         数量          |
| :------------: | :-----------------: | :-------------------: |
|       u4       |        magic        |           1           |
|       u2       |    minor_version    |           1           |
|       u2       |    major_version    |           1           |
|       u2       | constant_pool_count |           1           |
|    cp_info     |    constant_pool    | constant_pool_count-1 |
|       u2       |     access_flag     |           1           |
|       u2       |     this_class      |           1           |
|       u2       |     super_class     |           1           |
|       u2       |  interfaces_count   |           1           |
|       u2       |     interfaces      |   interfaces_count    |
|       u2       |    fields_count     |           1           |
|   field_info   |       fields        |     fields_count      |
|       u2       |    methods_count    |           1           |
|  method_info   |       methods       |     methods_count     |
|       u2       |  attributes_count   |           1           |
| attribute_info |     attributes      |   attributes_count    |

### 2.字节码指令

#### 2.1栈操作

&emsp;&emsp;约定：下面介绍的指令若用x代表数据类型，代表该指令支持多种数据类型，但并不代表支持所有类型，是否支持该类型看上图。

&emsp;&emsp;字节码执行模型：JVM是一台基于栈的计算器。每个线程都有一个独立的线程栈，用于存储栈帧。每一次方法调用都会自动创建一个栈帧。栈帧由操作数栈，局部变量数组，以及一个class引用构成。详细的介绍看第四章字节码执行引擎。

- 将一个局部变量加载到操作栈：xload，xload_< n>
- 将一个数值从操作数栈存储到操作栈：xstore，xstore_< n>
- 将一个常量加载到操作数栈：xipush，xconst，ldc
- 扩充局部变量表的访问索引指令：wide
- 复制栈顶的值，并加复制的值压入栈：dup
- 删除栈顶元素：pop
- 交换两个栈顶元素：swap
- 复制栈顶的值，并加复制的值压入最上面两个值的下方：dup_x1

- 复制栈顶 1个64位/或2个32位的值, 并将复制的值按照原始顺序，插入原
  始值下面一个32位值的下方：dup2_x1

#### 2.2运算和类型转换指令

- 加法：xadd
- 减法：xsub
- 乘法：xmul
- 除法：xdiv
- 求余：xrem
- 取反：xneg
- 位移：xshl，xshr
- 按位或：xor
- 按位与：xand
- 按位异或：xxor
- 变量自增：iinc
- 比较：lcmp，fcmpl，dcmpl
- 类型转换：i2b，i2c，i2s，f2i，ftl

唯一不需要将数值load到操作数栈的指令是 iinc ，它可以直接对 LocalVariableTable 中的值进行运算。 其他的所有操作均使用栈来执行。

#### 2.3对象操作

- 创建类实例：new
- 创建数组：newarray，anewarray，multianewarray
- 访问类字段：getstatic，putstatic
- 访问实例字段：getfiled，pushfiled
- 加载数组元素到操作栈：xaload
- 将操作栈值存储到数组：xastore
- 取数组长度：arraylength
- 检查类实例类型：instanceof，checkcast

#### 2.4 程序流程控制

- 条件分支
- 复合条件分支：tableswitch，lookupswitch
- 无条件分支：goto

#### 2.5 方法调用和返回

- 调用对象实例：invokevirtual
- 调用接口：invokeinterface
- 调用一些特殊的实例方法：invokespecial，如实例初始化方法，私有方法，父类方法
- 调用类静态方法：invokestatic
- invokedynamic（还不太理解）

&emsp;&emsp;Java虚拟机的字节码指令集在JDK7之前一直就只有前面提到的4种指令（invokestatic，invokespecial，invokevirtual，invokeinterface）。随着JDK 7的发布，字节码指令集新增了invokedynamic 指令。这条新增加的指令是实现“动态类型语言”（Dynamically Typed Language）支持而进行的改进之一，同时也是JDK 8以后
&emsp;&emsp;支持的lambda表达式的实现基础。
&emsp;&emsp;为什么要新增加一个指令呢？
&emsp;&emsp;我们知道在不改变字节码的情况下，我们在Java语言层面想调用一个类A的方法m，只有两个办法：使用A a=new A(); a.m() ，拿到一个A类型的实例，然后直接调用方法；通过反射，通过A.class.getMethod拿到一个Method，然后再调用这个Method.invoke 反射调用。这两个方法都需要显式的把方法m和类型A直接关联起来，假设有一个类型B，也有一个一模一样的方法签名的m方法，怎么来用这个方法在运行期指定调用A或者B的m方法呢？这个操作在JavaScript这种基于原型的语言里或者是C#这种有函数指针/方法委托的语言里非常常见，Java里是没有直接办法的。Java里我们一般建议使用一个A和B公有的接口IC，然后IC里定义方法m，A和B都实现接口IC，这样就可以在运行时把A和B都当做IC类型来操作，就同时有了方法m，这样的“强约束”带来了很多额外的操作。
&emsp;&emsp;而新增的invokedynamic指令，配合新增的方法句柄（Method Handles，它可以用来描述一个跟类型A无关的方法m的签名，甚至不包括方法名称，这样就可以做到我们使用方法m的签名，但是直接执行的时候调用的是相同签名的另一个方法b），可以在运行时再决定由哪个类来接收被调用的方法。在此之前，只能使用反射来实现类似的功能。该指令使得可以出现基于JVM的动态语言，让jvm更加强大。而且在JVM上实现动态调用机制，不会破坏原有的调用机制。这样既很好的支持了Scala、Clojure这些JVM上的动态语言，又可以支持代码里的动态lambda表达式。

### 3. 类加载器

&emsp;&emsp;从Java虚拟机的角度来看，只存在两种不同的类加载器：一种是启动类加载器（BootstrapClassLoader），这个类加载器使用C++语言实现，是虚拟机自身的一部分；另外一种就是其他所有的类加载器，这些类加载器都由Java语言实现，独立存在于虚拟机外部，并且全都继承自抽象类java.lang.ClassLoader。从在Java开发人员的角度来看，类加载器就应当划分得更细致一些。自JDK 1.2以来，Java一直保持着三层类加载器、双亲委派的类加载架构。

&emsp;&emsp;三层类加载器：

- 启动类加载器：它用来加载 Java 的核心类，是用原生C++代码来实现的，并不继承自 java.lang.ClassLoader（负责加载JDK中jre/lib/rt.jar里所有的class）。它可以看做是JVM自带的，我们再代码层面无法直接获取到启动类加载器的引用，所以不允许直接操作它， 如果打印出来就是个null 。举例来说，java.lang.String是由启动类加载器加载的，所以String.class.getClassLoader()就会返回null。
- 扩展类加载器：它负责加载JRE的扩展目录，lib/ext或者由java.ext.dirs系统属性指定的目录中的JAR包的类，代码里直接获取它的父类加载器为null（因为无法拿到启动类加载器）。
- 应用类加载器：它负责在JVM启动时加载来自Java命令的classpath或者cp选项、java.class.path系统属性指定的jar包和类路径。在应用程序代码里可以通过ClassLoader的静态方法getSystemClassLoader()来获取应用类加载器。如果没有特别指定，则在没有使用自定义类加载器情况下，用户自定义的类都由此加载器加载。

此外还可以自定义类加载器。如果用户自定义了类加载器，则自定义类加载器都以应用类加载器作为父加载器。应用类加载器的父类加载器为扩展类加载器。这些类加载器是有层次关系的，启动加载器又叫根加载器，是扩展加载器的父加载器，但是直接从ExClassLoader里拿不到它的引用，同样会返回null。

类加载机制有三个特点：

1. 双亲委托：当一个自定义类加载器需要加载一个类，比如java.lang.String，它很懒，不会一上来就直接试图加载它，而是先委托自己的父加载器去加载，父加载器如果发现自己还有父加载器，会一直往前找，这样只要上级加载器，比如启动类加载器已经加载了某个类比如java.lang.String，所有的子加载器都不需要自己加载了。如果几个类加载器都没有加载到指定名称的类，那么会抛出ClassNotFountException异常。
2. 负责依赖：如果一个加载器在加载某个类的时候，发现这个类依赖于另外几个类或接口，也会去尝试加载这些依赖项。
3. 缓存加载：为了提升加载效率，消除重复加载，一旦某个类被一个类加载器加载，那么它会缓存这个加载结果，不会重复加载。