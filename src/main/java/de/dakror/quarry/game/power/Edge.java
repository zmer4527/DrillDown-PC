package de.dakror.quarry.game.power;

import de.dakror.quarry.structure.base.Structure;
import java.util.Objects;

public class Edge {
   private Structure<?> a;
   private Structure<?> b;
   private int dockA;
   private int dockB;
   private PowerNetwork.NetworkStrength networkStrength;

   protected Edge(Structure<?> a, int dockA, Structure<?> b, int dockB, PowerNetwork.NetworkStrength networkStrength) {
      this.a = a;
      this.b = b;
      this.dockA = dockA;
      this.dockB = dockB;
      this.networkStrength = networkStrength;
   }

   public PowerNetwork.NetworkStrength getNetworkStrength() {
      return this.networkStrength;
   }

   public int getDockA() {
      return this.dockA;
   }

   public int getDockB() {
      return this.dockB;
   }

   public Structure<?> getOther(Structure<?> s) {
      if (s.equals(this.a)) {
         return this.b;
      } else {
         return s.equals(this.b) ? this.a : null;
      }
   }

   public Structure<?> getA() {
      return this.a;
   }

   public Structure<?> getB() {
      return this.b;
   }

   public void setA(Structure<?> a) {
      this.a = a;
   }

   public void setB(Structure<?> b) {
      this.b = b;
   }

   public void setDockA(int dockA) {
      this.dockA = dockA;
   }

   public void setDockB(int dockB) {
      this.dockB = dockB;
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.a, this.dockA, this.b, this.dockB) + Objects.hash(this.b, this.dockB, this.a, this.dockA);
   }

   @Override
   public String toString() {
      return String.format("%d:%d <-> %d:%d", this.a.getIndex(), this.dockA, this.b.getIndex(), this.dockB);
   }

   @Override
   public boolean equals(Object obj) {
      return obj instanceof Edge ? obj.hashCode() == this.hashCode() : false;
   }
}
