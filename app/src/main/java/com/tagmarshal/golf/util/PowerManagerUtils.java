package com.tagmarshal.golf.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.os.PowerManager;

/**
 * 通过java反射调用未对外开放的上电函数
 */
public class PowerManagerUtils {
	private static final String CLASS = "android.os.PowerManager";
	private static final String UART_POWER_OPEN = "modulePower";

	/**
	 *uart_num：UHF模块上电为0x02,模块中断输出脚控制为0x0C
	 * 433模块m0脚控制为0x16，模块m1脚控制为0x17
	 */
	public static void open(PowerManager manager, int uart_num) {
		try {
			Class<?> spClass = Class.forName(CLASS);

			Method getMethod = spClass.getMethod(UART_POWER_OPEN, int.class,
					boolean.class);
			getMethod.invoke(manager, uart_num, true);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
	}

	/**
	 *uart_num：UHF模块上电为0x02,模块中断输出脚控制为0x0C
	 * 433模块m0脚控制为0x16，模块m1脚控制为0x17
	 */
	public static void close(PowerManager manager, int uart_num) {
		try {
			Class<?> spClass = Class.forName(CLASS);

			Method getMethod = spClass.getMethod(UART_POWER_OPEN, int.class,
					boolean.class);
			getMethod.invoke(manager, uart_num, false);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
	}
}
