package de.dakror.quarry.game.power;

import com.badlogic.gdx.utils.Array;
import de.dakror.quarry.structure.base.Structure;

public class PowerGrid {
   private Array<PowerNetwork> networks = new Array();

   public Array<PowerNetwork> getNetworks() {
      return this.networks;
   }

   public void update(double deltaTime, int gameSpeed) {
      for (PowerNetwork n : this.networks) {
         n.update(deltaTime, gameSpeed);
      }
   }

   public void clearHighPowerCache() {
      for (int i = 0; i < this.networks.size; i++) {
         ((PowerNetwork)this.networks.get(i)).clearHighPowerCache();
      }
   }

   public void clear() {
      this.networks.clear();
   }

   public void addNetwork(PowerNetwork network) {
      this.networks.add(network);
      this.clearHighPowerCache();
   }

   public void removeNetwork(PowerNetwork network) {
      this.networks.removeValue(network, true);
      this.clearHighPowerCache();
   }

   public boolean mergeNetworks(PowerNetwork a, PowerNetwork b) {
      if (a == b) {
         return false;
      } else {
         if (!b.hasEdges()) {
            Structure<?> vert = b.getFirstVertex();
            if (vert != null) {
               a.addVertex(vert);
            }
         }

         for (Edge e : b.getEdges()) {
            a.addEdge(e);
         }

         this.removeNetwork(b);
         return true;
      }
   }

   @Override
   public String toString() {
      return this.networks.toString();
   }
}
