# AndroidIDE Version `v20251104` Changelog

以下是对新版本大概描述。

## 新增功能

 1.启动动画：增加Lottie动画，用于启动界面（后续也可以考虑用于其它界面）
             1.1：在设置页面增加Lottie动画设置选项，用于切换动画，与导入导出。
             2.2：感谢这位伙伴，来自tg群组：@socex4_1    #它为此贡献了两个Lottie动画文件，以及土库曼语的语言文件
             
 2.权限申请界面：增加一键申请权限按钮，适配各家os系统特性，排队申请权限。这样更方便
 3.崩溃界面：增加按钮：复制日志，重启界面，退出APP
 3.编辑器界面底部符号输入的工具栏增加上下左右这四个光标移动符号，分为长按移动，点击移动
 4.action菜单：增加光标移动，以及其它菜单功能
 5.新增：类似Android studio的sdkmanager功能。还需要升级调整，定义为专属sdk管理
 6.git版本控制系统：
 6.1 项目页面初步完善。新增了刷新文件，定位文件，收展文件列表节点功能（分为两个方式：点击折叠全部节点，长按展开全部节点）
## 重做的功能

1.重做editor菜单jump Line，增加横向Line（横向列）。现在支持横向列+纵向行的跳转到行
2.重做自动保存：，重做自动保存，新增加支持设置保存时间，允许使用者自由设置任何时间。支持微妙到小时之间全部单位时间设置
3.重做升级模板列表fragment：升级支持类别选项卡。其中选项卡是动态同步模板数据元，指定类别添加到指定选项卡中。然后增加支持列表网格样式自定义，默认四宫格类型
4.调整PreviewLayoutAction：调整PreviewLayoutAction为新版布局编辑器，将旧版布局编辑器完全归档删除。重做新版布局编辑器部分源码以便于适配IDE运行时环境
5.DataFileTreeFragment (重构版)
 * 不需要任何挂载操作，直接访问应用程序的内部私有数据目录。
 * 它可以像 Root 权限一样管理自己的数据目录 (/data/data/com.itsaky.androidide/) 以及外部私有目录。
 6.为文件列表非特定图标的文件类型设置attr主题颜色
 
 
## Removals or remove

1.启动界面：移除com.itsaky.androidide.fragments.onboarding.StatisticsFragment，并设置默认false。
            这个fragment已经毫无用处，所以需要移除。
2.移除设置>开发者选项>匿名数据收集：全部移除，这个功能毫无用处

## Bug fixes

1.启动界面：因为移除StatisticsFragment，导致tryNavigateToMainIfSetupIsCompleted的布尔永远为false
            所以替换成其它fragment来更新tryNavigateToMainIfSetupIsCompleted的状态

2.优化是否编辑状态的标记：原先逻辑通过一个简单的布尔标签来管理，存在许多不便，不能有效感知是否编辑状态。
   fix方法：通过获取一个历史栈指针，将其存储为一个变量。这个指针状态比喻：未修改时指针状态就存储为0:0，当有任何编辑时指针就可能会存储为0:1(这里我设想成0:1，他也可能是0:-1)。
   当用户点击撤回时，如果撤回干净指针就回归0:0（未修改状态），如果未撤回干净或者点击重做那么指针依旧存储为0:1。以此类推，相比之前通过标签管理，这个历史栈指针更加完善，更智能感知是否编辑状态。
3.修复tooling服务器的UnsupportedMethodException: Library.getSrcJars()问题：因为升级到9.0版本缘故，对8x的srcjar和9x的srcjar没做兼容处理导致。
4.修复了自动保存未按照计算器时间保存文件问题。
4.修复权限申请fragment：权限旧的os系统判断改用全新的os系统判断，并修复通知权限申请问题。细分权限申请
5.修复MatchPredicate.doPredicateInternal的Index 655279 out引起的闪退。
## sdk变动

kotlin和ksp改为：2.2.0
agp改为8.13.0


#构建系统服务器升级情况：

tooling降级到8.9，目前最大支持8.14项目构建。

#```# xml模块升级

1.aapt等源码更新到最新版本

2.其它源码整体更改为9x的代码适配


# 新增模块：
1.editor lsp4j模块
2.增加layout编辑器
3.增加git模块