package de.dakror.common.debug;

public class VariableNode {
   boolean couldHaveChildren;
   Object realObject;
   String name;
   boolean empty;

   public VariableNode(String name, Object o) {
      this.name = name;
      this.realObject = o;
      this.couldHaveChildren = this.realObject != null && (this.realObject.getClass().isArray() || !TreeDebugger.isWrapperType(this.realObject.getClass()));
   }

   public boolean isEmpty() {
      return this.empty;
   }

   public void setEmpty(boolean empty) {
      this.empty = empty;
   }

   public Object getRealObject() {
      return this.realObject;
   }

   public boolean couldHaveChildren() {
      return this.couldHaveChildren;
   }

   public String getName() {
      return this.name;
   }

   @Override
   public String toString() {
      return this.realObject == null ? this.name + " = null" : this.name + " = " + this.realObject.toString();
   }
}
