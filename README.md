#来把锁2.0App
客户端分乘务员、乘客两个模块。
基于蓝牙2.0 实现硬件与手机持续连接，可以手动断开续连，主要实现方法：Android系统广播，服务，多线程，Handler，手机与硬件不停收发数据，一方如果5s内没有收到数据，将会报警，蜂鸣器发出声响，手机闪光灯不断闪烁，推送出具体乘客箱包危险的信息，智能语音提示.
服务器使用LeanCloud 存储和推送，使用第三方的ZXing实现二维码扫描功能
界面使用开源的EventButton，乘务员模块使用类似于微博主界面，ViewPager,Fragment，个人独立开发
##不足
线程管理不规范，包命名规则不规范，UI有待于性能优化，没有架构，耦合性高，过长的冗余代码
