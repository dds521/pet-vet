package com.petvetai.app.controller;
import java.lang.ref.*;

/**
 * Java四种引用类型示例
 * 
 * 记忆口诀：
 * 1. 强引用 - 普通对象，GC永不回收（只要可达）
 * 2. 软引用 - 内存充足时保留，不足时回收（缓存场景）
 * 3. 弱引用 - 一旦没有强引用，立即回收（WeakHashMap场景）
 * 4. 虚引用 - 对象回收前通知，必须配合引用队列（对象回收监听）
 */
public class JavaReferenceTypesDemo {
    
    public static void main(String[] args) {
        System.out.println("========== Java四种引用类型示例 ==========\n");
        
        // 1. 强引用
        demonstrateStrongReference();
        
        // 2. 软引用
        demonstrateSoftReference();
        
        // 3. 弱引用
        demonstrateWeakReference();
        
        // 4. 虚引用
        demonstratePhantomReference();
    }
    
    /**
     * 1. 强引用 (Strong Reference)
     * 
     * 类比：普通关系 - 只要人在，关系就在
     * 特点：只要强引用存在，GC绝不会回收对象
     * 应用：99%的代码都是强引用
     */
    private static void demonstrateStrongReference() {
        System.out.println("【1. 强引用 - Strong Reference】");
        System.out.println("类比：普通关系 - 只要人在，关系就在\n");
        
        // 普通对象赋值就是强引用
        String str = new String("我是强引用对象");
        String str2 = str;  // str2也是强引用
        
        System.out.println("创建对象：" + str);
        System.out.println("只要 str 或 str2 存在，对象就不会被GC回收");
        System.out.println("只有设置为 null，才会失去引用");
        
        str = null;  // 失去一个强引用
        System.out.println("str = null 后，str2 仍持有强引用，对象依然存在");
        System.out.println("str2 = " + str2);
        
        str2 = null;  // 失去所有强引用
        System.out.println("str2 = null 后，对象失去所有强引用，下次GC会被回收");
        System.out.println("---\n");
    }
    
    /**
     * 2. 软引用 (Soft Reference)
     * 
     * 类比：好朋友关系 - 平时都在一起，但遇到困难（内存不足）时会暂时分开
     * 特点：内存充足时保留，内存不足时回收（OOM前回收）
     * 应用：缓存场景（如图片缓存）
     */
    private static void demonstrateSoftReference() {
        System.out.println("【2. 软引用 - Soft Reference】");
        System.out.println("类比：好朋友 - 平时在一起，困难时（内存不足）会分开\n");
        
        // 创建强引用对象
        String strongRef = new String("原始数据");
        System.out.println("创建强引用：" + strongRef);
        
        // 创建软引用
        SoftReference<String> softRef = new SoftReference<>(strongRef);
        System.out.println("创建软引用：" + softRef.get());
        
        // 移除强引用
        strongRef = null;
        System.out.println("移除强引用后，软引用仍然可以访问：" + softRef.get());
        
        // 注意：软引用在内存充足时不会回收
        // 只有在内存不足时（快要OOM），JVM才会回收软引用指向的对象
        System.out.println("软引用特点：");
        System.out.println("  - 内存充足：对象保留，可以通过 softRef.get() 获取");
        System.out.println("  - 内存不足：JVM在OOM前回收，softRef.get() 返回 null");
        System.out.println("  - 适用场景：缓存（如图片缓存、数据缓存）");
        System.out.println("---\n");
    }
    
    /**
     * 3. 弱引用 (Weak Reference)
     * 
     * 类比：点头之交 - 一旦对方消失（强引用消失），关系就断了
     * 特点：一旦没有强引用，GC立即回收（下一次GC就会回收）
     * 应用：WeakHashMap、ThreadLocal、防止内存泄漏
     */
    private static void demonstrateWeakReference() {
        System.out.println("【3. 弱引用 - Weak Reference】");
        System.out.println("类比：点头之交 - 一旦对方消失（强引用消失），关系就断了\n");
        
        // 创建强引用对象
        String strongRef = new String("临时数据");
        System.out.println("创建强引用：" + strongRef);
        
        // 创建弱引用
        WeakReference<String> weakRef = new WeakReference<>(strongRef);
        System.out.println("创建弱引用：" + weakRef.get());
        
        // 移除强引用
        strongRef = null;
        System.out.println("移除强引用后，弱引用暂时还能访问：" + weakRef.get());
        
        // 手动触发GC（仅用于演示，实际开发中不要手动GC）
        System.gc();
        
        try {
            Thread.sleep(100);  // 给GC一点时间
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        // 弱引用指向的对象应该被回收了
        String result = weakRef.get();
        System.out.println("GC后，弱引用对象：" + (result == null ? "已被回收（null）" : "仍存在"));
        
        System.out.println("弱引用特点：");
        System.out.println("  - 一旦强引用消失，下一次GC立即回收");
        System.out.println("  - 适用场景：WeakHashMap、防止内存泄漏、临时缓存");
        System.out.println("---\n");
    }
    
    /**
     * 4. 虚引用 (Phantom Reference)
     * 
     * 类比：鬼魂关系 - 你看不到它（get()总是null），但它死了会通知你
     * 特点：get()总是返回null，对象回收前会收到通知（通过引用队列）
     * 应用：对象回收监听、精确的内存管理、DirectByteBuffer等
     */
    private static void demonstratePhantomReference() {
        System.out.println("【4. 虚引用 - Phantom Reference】");
        System.out.println("类比：鬼魂 - 看不到（get()=null），但死了会通知你\n");
        
        // 创建引用队列
        ReferenceQueue<String> queue = new ReferenceQueue<>();
        
        // 创建强引用对象
        String strongRef = new String("重要数据");
        System.out.println("创建强引用：" + strongRef);
        
        // 创建虚引用（必须配合引用队列）
        PhantomReference<String> phantomRef = new PhantomReference<>(strongRef, queue);
        
        // 注意：虚引用的get()总是返回null！
        System.out.println("虚引用 get()：" + phantomRef.get() + " (总是null)");
        
        // 移除强引用
        strongRef = null;
        System.out.println("移除强引用");
        
        // 手动触发GC
        System.gc();
        
        try {
            Thread.sleep(100);
            
            // 检查引用队列，看是否有对象被回收
            Reference<? extends String> ref = queue.poll();
            if (ref != null) {
                System.out.println("检测到对象被回收！虚引用进入队列：" + ref);
            } else {
                System.out.println("引用队列为空（可能还未回收，或需要更多时间）");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        System.out.println("虚引用特点：");
        System.out.println("  - get() 永远返回 null");
        System.out.println("  - 必须配合 ReferenceQueue 使用");
        System.out.println("  - 对象回收前会进入队列，可以收到回收通知");
        System.out.println("  - 适用场景：对象回收监听、DirectByteBuffer、精确内存管理");
        System.out.println("---\n");
    }
}

/**
 * ========== 快速记忆总结 ==========
 * 
 * 1. 强引用 (Strong)
 *    - 特点：GC永不回收（只要可达）
 *    - 代码：String s = new String("xxx")
 *    - 记忆：普通人际关系
 * 
 * 2. 软引用 (Soft)
 *    - 特点：内存不足时回收（OOM前）
 *    - 代码：SoftReference<String> ref = new SoftReference<>(obj)
 *    - 记忆：好朋友，困难时会分开
 *    - 用途：缓存
 * 
 * 3. 弱引用 (Weak)
 *    - 特点：强引用消失后，下次GC立即回收
 *    - 代码：WeakReference<String> ref = new WeakReference<>(obj)
 *    - 记忆：点头之交，人走茶凉
 *    - 用途：WeakHashMap、防内存泄漏
 * 
 * 4. 虚引用 (Phantom)
 *    - 特点：get()=null，回收前通知
 *    - 代码：PhantomReference<String> ref = new PhantomReference<>(obj, queue)
 *    - 记忆：鬼魂，看不到但会通知
 *    - 用途：回收监听、DirectByteBuffer
 * 
 * ========== 引用强度对比 ==========
 * 强引用 > 软引用 > 弱引用 > 虚引用
 * 
 * ========== GC回收时机 ==========
 * 强引用：永不回收（只要可达）
 * 软引用：内存不足时回收
 * 弱引用：强引用消失后，立即回收
 * 虚引用：强引用消失后，回收前通知
 */
