package tcb;

import java.util.concurrent.Executors;

import org.jinterop.dcom.common.JIException;
import org.jinterop.dcom.core.JIString;
import org.jinterop.dcom.core.JIVariant;
import org.openscada.opc.lib.common.ConnectionInformation;
import org.openscada.opc.lib.da.AccessBase;
import org.openscada.opc.lib.da.DataCallback;
import org.openscada.opc.lib.da.Item;
import org.openscada.opc.lib.da.ItemState;
import org.openscada.opc.lib.da.Server;
import org.openscada.opc.lib.da.SyncAccess;

public class UtgardTutorial1 {

	public static void main(String[] args) throws Exception {
		// 连接信息
		final ConnectionInformation ci = new ConnectionInformation(); 
		ci.setHost("127.0.0.1");
		//ci.setHost("192.168.10.34");// 电脑IP
		ci.setDomain("");                  // 域，为空就行
		ci.setUser("opcuser");             // 电脑上自己建好的用户名
		ci.setPassword("123456");          // 用户名的密码

		// 使用MatrikonOPC Server的配置
		//ci.setProgId("Matrikon.OPC.Simulation.1");
		//ci.setProgId("Word.Application");
		ci.setClsid("F8582CF3-88FB-11D0-B850-00C0F0104305"); // MatrikonOPC的注册表ID，可以在“组件服务”里看到
		//ci.setClsid("13486D44-4821-11D2-A494-3CB306C10000");
		final String itemId = "group1.tag1";    // 项的名字按实际

		// 使用KEPServer的配置
		// ci.setClsid("7BC0CC8E-482C-47CA-ABDC-0FE7F9C6E729"); // KEPServer的注册表ID，可以在“组件服务”里看到
		//final String itemId = "u.u.u";    // 项的名字按实际，没有实际PLC，用的模拟器：simulator
		// final String itemId = "通道 1.设备 1.标记 1";

		// 启动服务
		final Server server = new Server(ci, Executors.newSingleThreadScheduledExecutor());

		try {
			// 连接到服务
			System.out.println("--begin-connect-----");
			server.connect();
			System.out.println("---end--connect-----");
			// add sync access, poll every 500 ms，启动一个同步的access用来读取地址上的值，线程池每500ms读值一次
			// 这个是用来循环读值的，只读一次值不用这样
			final AccessBase access = new SyncAccess(server, 500);
			// 这是个回调函数，就是读到值后执行这个打印，是用匿名类写的，当然也可以写到外面去
			access.addItem(itemId, new DataCallback() {
				@Override
				public void changed(Item item, ItemState itemState) {
					int type = 0;
					try {
						type = itemState.getValue().getType(); // 类型实际是数字，用常量定义的
					} catch (JIException e) {
						e.printStackTrace();
					}
					System.out.println("监控项的数据类型是：-----" + type);
					System.out.println("监控项的时间戳是：-----" + itemState.getTimestamp().getTime());
					System.out.println("监控项的详细信息是：-----" + itemState);

					// 如果读到是short类型的值
					if (type == JIVariant.VT_I2) {
						short n = 0;
						try {
							n = itemState.getValue().getObjectAsShort();
						} catch (JIException e) {
							e.printStackTrace();
						}
						System.out.println("-----short类型值： " + n); 
					}

					// 如果读到是字符串类型的值
					if(type == JIVariant.VT_BSTR) {  // 字符串的类型是8
						JIString value = null;
						try {
							value = itemState.getValue().getObjectAsString();
						} catch (JIException e) {
							e.printStackTrace();
						} // 按字符串读取
						String str = value.getString(); // 得到字符串
						System.out.println("-----String类型值： " + str); 
					}
				}
			});
			// start reading，开始读值
			access.bind();
			// wait a little bit，有个10秒延时
			Thread.sleep(100 * 1000);
			// stop reading，停止读取
			access.unbind();
		} catch (final JIException e) {
			System.out.println("JIException:"+String.format("%08X: %s", e.getErrorCode(), server.getErrorMessage(e.getErrorCode())));
		    e.printStackTrace();
		} catch (final Exception e) {
			System.out.println("!Exception!"+e.getMessage());
		}
	}
}