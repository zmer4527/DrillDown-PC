package de.dakror.modding.stub;

import java.lang.instrument.Instrumentation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;

public class StubAgent {
   private static final String AGENT_CLASS = System.getProperty("de.dakror.modding.agent.class", "de.dakror.modding.agent.ModAgent");
   private static final String AGENT_URL = System.getProperty("de.dakror.modding.agent.url");
   private static final boolean IS_DEV = AGENT_URL != null;
   private static final ClassLoader platformLoader = getPlatformClassLoader();
   static final Map<String, StubAgent.Agent> agents = new HashMap<>();

   public static void premain(String agentArgs, Instrumentation inst) throws Throwable {
      for (StubAgent.Agent agent : agents.values()) {
         agent.premain(agentArgs, inst);
      }
   }

   public static void agentmain(String agentArgs, Instrumentation inst) throws Throwable {
      for (StubAgent.Agent agent : agents.values()) {
         agent.agentmain(agentArgs, inst);
      }
   }

   private static void findAgents() {
      URL stubLocation = StubAgent.class.getProtectionDomain().getCodeSource().getLocation();

      try {
         if (AGENT_URL != null) {
            tryLoading(stubLocation.toURI().resolve(AGENT_URL).toURL(), AGENT_CLASS);
         }

         tryLoading(null, AGENT_CLASS);
         if (AGENT_URL == null) {
            tryLoading(stubLocation.toURI().resolve("ModLoader.jar").toURL(), AGENT_CLASS);
         }

         tryLoading(stubLocation, AGENT_CLASS);
      } catch (Throwable var2) {
         System.err.print("While finding modloader agents: ");
         var2.printStackTrace();
      }
   }

   private static ClassLoader getPlatformClassLoader() {
      try {
         return (ClassLoader)ClassLoader.class.getMethod("getPlatformClassLoader").invoke(null);
      } catch (ReflectiveOperationException var1) {
         return StubAgent.class.getClassLoader().getParent();
      }
   }

   private static void tryLoading(URL url, String agentName) {
      ClassLoader loader = (ClassLoader)(url == null ? StubAgent.class.getClassLoader() : new StubAgent.StubLoader(new URL[]{url}, platformLoader));

      try {
         StubAgent.Agent agent = new StubAgent.Agent(loader.loadClass(agentName));
         agents.putIfAbsent(agent.getName(), agent);
      } catch (ClassNotFoundException var4) {
      } catch (Throwable var5) {
         Throwable e = var5;
         if (var5 instanceof InvocationTargetException) {
            e = ((InvocationTargetException)var5).getTargetException();
         }

         System.err.print("While loading agent " + agentName + " from " + url + ": ");
         e.printStackTrace();
      }
   }

   static {
      findAgents();
   }

   private static class Agent {
      public final Class<?> agentClass;

      private Agent(Class<?> agentClass) {
         this.agentClass = agentClass;
      }

      public void premain(Object... args) throws Throwable {
         this.safeCall("premain", args);
      }

      public void agentmain(Object... args) throws Throwable {
         this.safeCall("agentmain", args);
      }

      public String getName() throws Throwable {
         try {
            return (String)this.callMethod("getName");
         } catch (NoSuchMethodException var2) {
            return this.agentClass.getName();
         }
      }

      protected void safeCall(String methodName, Object... args) throws Throwable {
         try {
            this.callMethod(methodName, args);
         } catch (Throwable var4) {
            if (StubAgent.IS_DEV) {
               throw var4;
            }

            var4.printStackTrace();
         }
      }

      protected Object callMethod(String methodName, Object... args) throws Throwable {
         for (Method method : this.agentClass.getMethods()) {
            if (method.getName().equals(methodName) && Modifier.isStatic(method.getModifiers())) {
               try {
                  return method.invoke(null, args);
               } catch (IllegalAccessException | IllegalArgumentException var8) {
               } catch (InvocationTargetException var9) {
                  throw var9.getTargetException();
               }
            }
         }

         throw new NoSuchMethodException(methodName);
      }
   }

   private static class StubLoader extends URLClassLoader {
      public StubLoader(URL[] urls, ClassLoader parent) {
         super(urls, parent);
      }
   }
}
