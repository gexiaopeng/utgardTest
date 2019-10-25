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
		// ������Ϣ
		final ConnectionInformation ci = new ConnectionInformation(); 
		ci.setHost("127.0.0.1");
		//ci.setHost("192.168.10.34");// ����IP
		ci.setDomain("");                  // ��Ϊ�վ���
		ci.setUser("opcuser");             // �������Լ����õ��û���
		ci.setPassword("123456");          // �û���������

		// ʹ��MatrikonOPC Server������
		//ci.setProgId("Matrikon.OPC.Simulation.1");
		//ci.setProgId("Word.Application");
		ci.setClsid("F8582CF3-88FB-11D0-B850-00C0F0104305"); // MatrikonOPC��ע���ID�������ڡ���������￴��
		//ci.setClsid("13486D44-4821-11D2-A494-3CB306C10000");
		final String itemId = "group1.tag1";    // ������ְ�ʵ��

		// ʹ��KEPServer������
		// ci.setClsid("7BC0CC8E-482C-47CA-ABDC-0FE7F9C6E729"); // KEPServer��ע���ID�������ڡ���������￴��
		//final String itemId = "u.u.u";    // ������ְ�ʵ�ʣ�û��ʵ��PLC���õ�ģ������simulator
		// final String itemId = "ͨ�� 1.�豸 1.��� 1";

		// ��������
		final Server server = new Server(ci, Executors.newSingleThreadScheduledExecutor());

		try {
			// ���ӵ�����
			System.out.println("--begin-connect-----");
			server.connect();
			System.out.println("---end--connect-----");
			// add sync access, poll every 500 ms������һ��ͬ����access������ȡ��ַ�ϵ�ֵ���̳߳�ÿ500ms��ֵһ��
			// ���������ѭ����ֵ�ģ�ֻ��һ��ֵ��������
			final AccessBase access = new SyncAccess(server, 500);
			// ���Ǹ��ص����������Ƕ���ֵ��ִ�������ӡ������������д�ģ���ȻҲ����д������ȥ
			access.addItem(itemId, new DataCallback() {
				@Override
				public void changed(Item item, ItemState itemState) {
					int type = 0;
					try {
						type = itemState.getValue().getType(); // ����ʵ�������֣��ó��������
					} catch (JIException e) {
						e.printStackTrace();
					}
					System.out.println("���������������ǣ�-----" + type);
					System.out.println("������ʱ����ǣ�-----" + itemState.getTimestamp().getTime());
					System.out.println("��������ϸ��Ϣ�ǣ�-----" + itemState);

					// ���������short���͵�ֵ
					if (type == JIVariant.VT_I2) {
						short n = 0;
						try {
							n = itemState.getValue().getObjectAsShort();
						} catch (JIException e) {
							e.printStackTrace();
						}
						System.out.println("-----short����ֵ�� " + n); 
					}

					// ����������ַ������͵�ֵ
					if(type == JIVariant.VT_BSTR) {  // �ַ�����������8
						JIString value = null;
						try {
							value = itemState.getValue().getObjectAsString();
						} catch (JIException e) {
							e.printStackTrace();
						} // ���ַ�����ȡ
						String str = value.getString(); // �õ��ַ���
						System.out.println("-----String����ֵ�� " + str); 
					}
				}
			});
			// start reading����ʼ��ֵ
			access.bind();
			// wait a little bit���и�10����ʱ
			Thread.sleep(100 * 1000);
			// stop reading��ֹͣ��ȡ
			access.unbind();
		} catch (final JIException e) {
			System.out.println("JIException:"+String.format("%08X: %s", e.getErrorCode(), server.getErrorMessage(e.getErrorCode())));
		    e.printStackTrace();
		} catch (final Exception e) {
			System.out.println("!Exception!"+e.getMessage());
		}
	}
}