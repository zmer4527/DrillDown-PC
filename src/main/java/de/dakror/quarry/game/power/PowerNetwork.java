package de.dakror.quarry.game.power;

import com.badlogic.gdx.math.WindowedMean;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntMap;
import com.badlogic.gdx.utils.IntSet;
import com.badlogic.gdx.utils.OrderedSet;
import com.badlogic.gdx.utils.IntMap.Entry;
import com.badlogic.gdx.utils.OrderedSet.OrderedSetIterator;
import de.dakror.quarry.structure.base.Dock;
import de.dakror.quarry.structure.base.Structure;
import de.dakror.quarry.structure.power.CopperCable;
import de.dakror.quarry.structure.power.PowerPole;
import de.dakror.quarry.structure.power.PowerPoleGhost;
import de.dakror.quarry.structure.power.Substation;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.TreeSet;

public class PowerNetwork {
   private PowerGrid grid;
   private Set<Edge> edgeSet;
   private IntMap<Edge[]> edges;
   private IntMap<IntMap<Boolean>> highPowerConnections;
   private OrderedSet<Structure<?>> donorVertices;
   private OrderedSet<Structure<?>> receiverVertices;
   private OrderedSet<Structure<?>> neutralVertices;
   private OrderedSet<Structure<?>> allVertices;
   private int numLowReceiverSlices;
   private int numHighReceiverSlices;
   private boolean hadEdgeDeletion;
   private final WindowedMean inTickMean = new WindowedMean(60);
   private final WindowedMean outTickMean = new WindowedMean(60);
   private final WindowedMean timeTickMean = new WindowedMean(60);
   double inTick;
   double outTick;
   double accumulatedOfferedPower;
   static int idCounter = 0;
   int id;
   final IntSet dfsScratch = new IntSet();
   Array<Edge> minimumSpanningTree = new Array();
   boolean minimumSpanningTreeDirty = true;

   public PowerNetwork(PowerGrid grid) {
      this.grid = grid;
      this.id = idCounter++;
      this.edges = new IntMap();
      this.edgeSet = new HashSet<>();
      this.neutralVertices = new OrderedSet();
      this.allVertices = new OrderedSet();
      this.highPowerConnections = new IntMap();
      this.donorVertices = new OrderedSet();
      this.receiverVertices = new OrderedSet();
   }

   public Structure<?> getFirstVertex() {
      return this.allVertices.size == 0 ? null : (Structure)this.allVertices.iterator().next();
   }

   public boolean hasVertex(Structure<?> s) {
      return this.allVertices.contains(s);
   }

   public PowerGrid getPowerGrid() {
      return this.grid;
   }

   public Set<Edge> getEdges() {
      return this.edgeSet;
   }

   public boolean addVertex(Structure<?> node) {
      if (!this.allVertices.add(node)) {
         return false;
      } else if (node.getDonorPriority() > 0 && !this.donorVertices.add(node)) {
         return false;
      } else {
         if (node.getReceiverPriority() > 0) {
            if (!this.receiverVertices.add(node)) {
               return false;
            }

            if (node.getSchema().highPower) {
               this.numHighReceiverSlices = this.numHighReceiverSlices + node.getReceiverPriority();
            }

            if (node.getSchema().lowPower) {
               this.numLowReceiverSlices = this.numLowReceiverSlices + node.getReceiverPriority();
            }
         }

         if (node.getDonorPriority() == 0 && node.getReceiverPriority() == 0 && !this.neutralVertices.add(node)) {
            return false;
         } else {
            if (!this.edges.containsKey(node.getIndex())) {
               this.edges.put(node.getIndex(), new Edge[node.getSchema().powerDocks]);
            }

            if (node.getSchema().highPower && !this.highPowerConnections.containsKey(node.getIndex())) {
               this.highPowerConnections.put(node.getIndex(), new IntMap());
            }

            node.setPowerNetwork(this);
            this.minimumSpanningTreeDirty = true;
            return true;
         }
      }
   }

   public Edge addEdge(Edge e) {
      return this.addEdge(e.getA(), e.getDockA(), e.getB(), e.getDockB(), e.getNetworkStrength());
   }

   public Edge addEdge(Structure<?> a, int dockA, Structure<?> b, int dockB, PowerNetwork.NetworkStrength networkStrength) {
      if (a == b) {
         return null;
      } else {
         int keyA = a.getIndex();
         Edge[] edgesOfA = (Edge[])this.edges.get(keyA);
         int keyB = b.getIndex();
         Edge[] edgesOfB = (Edge[])this.edges.get(keyB);
         if ((edgesOfA == null || edgesOfA[dockA] == null) && (edgesOfB == null || edgesOfB[dockB] == null)) {
            Edge e = new Edge(a, dockA, b, dockB, networkStrength);
            this.edgeSet.add(e);
            this.addVertex(a);
            this.addVertex(b);
            edgesOfA = (Edge[])this.edges.get(keyA);
            edgesOfA[dockA] = e;
            this.edges.put(keyA, edgesOfA);
            edgesOfB = (Edge[])this.edges.get(keyB);
            edgesOfB[dockB] = e;
            this.edges.put(keyB, edgesOfB);
            if (networkStrength == PowerNetwork.NetworkStrength.PowerPole) {
               IntMap<Boolean> connectionsA = (IntMap<Boolean>)this.highPowerConnections.get(keyA);
               connectionsA.put(keyA, true);
               IntMap<Boolean> connectionsB = (IntMap<Boolean>)this.highPowerConnections.get(keyB);
               connectionsB.put(keyB, true);
               this.grid.clearHighPowerCache();
            }

            return e;
         } else {
            return null;
         }
      }
   }

   public boolean removeVertex(Structure<?> node, boolean createIslandNetworks) {
      if (!this.allVertices.remove(node)) {
         return false;
      } else if (node.getDonorPriority() > 0 && !this.donorVertices.remove(node)) {
         return false;
      } else {
         if (node.getReceiverPriority() > 0) {
            if (!this.receiverVertices.remove(node)) {
               return false;
            }

            if (node.getSchema().highPower) {
               this.numHighReceiverSlices = this.numHighReceiverSlices + node.getReceiverPriority();
            }

            if (node.getSchema().lowPower) {
               this.numLowReceiverSlices = this.numLowReceiverSlices + node.getReceiverPriority();
            }
         }

         if (node.getDonorPriority() == 0 && node.getReceiverPriority() == 0 && !this.neutralVertices.remove(node)) {
            return false;
         } else {
            Edge[] edgesToRemove = (Edge[])this.edges.get(node.getIndex());

            for (Edge e : edgesToRemove) {
               if (e != null) {
                  this.removeEdge(e, createIslandNetworks);
               }
            }

            this.edges.remove(node.getIndex());
            return true;
         }
      }
   }

   public void offerPower(double deltaTime, int gameSpeed, double power, Structure<?> donor) {
      if (donor.getSchema().highPower) {
         double remainder = power;
         int donorPriority = donor.getDonorPriority();
         int receiverSlices = this.numHighReceiverSlices;

         for (int i = 0; i < 5 && remainder != 0.0; i++) {
            OrderedSetIterator var12 = this.receiverVertices.iterator();

            while (var12.hasNext()) {
               Structure<?> n = (Structure<?>)var12.next();
               if (donorPriority <= 0 || n.getDonorPriority() <= donorPriority) {
                  double piece = Math.min(remainder / (double)receiverSlices * (double)n.getReceiverPriority(), remainder);
                  PowerNetwork.NetworkStrength strength = this.getConnectionNetworkStrength(donor, n);
                  if (strength != null) {
                     double networkStrength = strength.maxPowerPerSecond * deltaTime * (double)gameSpeed;
                     double leftover = n.acceptPower(piece, networkStrength);
                     if (!(n instanceof Substation)) {
                        this.outTick += piece - leftover;
                     }

                     remainder = remainder - piece + leftover;
                     if (remainder == 0.0) {
                        break;
                     }
                  }
               }
            }
         }

         this.inTick += power - remainder;
      } else {
         this.accumulatedOfferedPower += power;
      }
   }

   private void distributeOfferedLowPower(double deltaTime, int gameSpeed) {
      double networkStrength = PowerNetwork.NetworkStrength.CopperCable.maxPowerPerSecond * deltaTime * (double)gameSpeed;
      double remainder = this.accumulatedOfferedPower;
      int receiverSlices = this.numLowReceiverSlices;

      for (int i = 0; i < 5 && remainder != 0.0; i++) {
         OrderedSetIterator var10 = this.receiverVertices.iterator();

         while (var10.hasNext()) {
            Structure<?> n = (Structure<?>)var10.next();
            if (remainder != 0.0) {
               double piece = Math.min(remainder / (double)receiverSlices * (double)n.getReceiverPriority(), remainder);
               double leftover = n.acceptPower(piece, networkStrength);
               if (!(n instanceof Substation)) {
                  this.outTick += piece - leftover;
               }

               remainder = remainder - piece + leftover;
               if (remainder != 0.0) {
                  continue;
               }
            }
         }
      }

      this.inTick = this.inTick + (this.accumulatedOfferedPower - remainder);
      this.accumulatedOfferedPower = remainder;
   }

   private void distributeStoredPower(double deltaTime, int gameSpeed) {
      OrderedSetIterator var4 = this.receiverVertices.iterator();

      while (var4.hasNext()) {
         Structure<?> r = (Structure<?>)var4.next();
         double delta = r.getPowerCapacity() - r.getPowerLevel();
         if (delta > 0.0) {
            double remainingRequest = delta;
            OrderedSetIterator var10 = this.donorVertices.iterator();

            while (var10.hasNext()) {
               Structure<?> d = (Structure<?>)var10.next();
               if (remainingRequest == 0.0) {
                  break;
               }

               if (d != r
                  && d.getReceiverPriority() <= r.getReceiverPriority()
                  && (d.getReceiverPriority() != r.getReceiverPriority() || !(r.getPowerRatio() >= d.getPowerRatio()))) {
                  double request = remainingRequest;
                  if (d.getReceiverPriority() == r.getReceiverPriority()) {
                     request = Math.min((d.getPowerRatio() - r.getPowerRatio()) / 2.0 * r.getPowerCapacity(), remainingRequest);
                  }

                  if (!(request < 1.0)) {
                     PowerNetwork.NetworkStrength strength = this.getConnectionNetworkStrength(d, r);
                     if (strength != null) {
                        double networkStrength = strength.maxPowerPerSecond * deltaTime * (double)gameSpeed;
                        request = Math.min(request, networkStrength);
                        double gotten = d.requestPower(request, networkStrength);
                        if (gotten > 0.0) {
                           double leftover = r.acceptPower(gotten, networkStrength);
                           d.refundPower(leftover);
                           if (!(r instanceof Substation)) {
                              this.outTick += gotten - leftover;
                           }

                           remainingRequest -= gotten - leftover;
                        }
                     }
                  }
               }
            }
         }
      }
   }

   public void update(double deltaTime, int gameSpeed) {
      if (this.isEmpty()) {
         this.grid.removeNetwork(this);
      } else {
         if (this.hadEdgeDeletion) {
            this.manageConnectedness();
            this.hadEdgeDeletion = false;
         }

         if (gameSpeed != 0) {
            this.timeTickMean.addValue((float)deltaTime);
            if (this.accumulatedOfferedPower > 0.0) {
               this.distributeOfferedLowPower(deltaTime, gameSpeed);
            }

            this.distributeStoredPower(deltaTime, gameSpeed);
            this.inTickMean.addValue((float)this.inTick);
            this.outTickMean.addValue((float)this.outTick);
            this.inTick = 0.0;
            this.outTick = 0.0;
         }
      }
   }

   public PowerNetwork.NetworkStrength getConnectionNetworkStrength(Structure<?> a, Structure<?> b) {
      if (a.getSchema().highPower && b.getSchema().highPower) {
         IntMap<Boolean> connectionsA = (IntMap<Boolean>)this.highPowerConnections.get(a.getIndex());
         if (connectionsA == null) {
            return null;
         } else {
            Boolean connectionA = (Boolean)connectionsA.get(b.getIndex());
            if (connectionA == null) {
               this.dfsScratch.clear();
               return this.findConnection(a, b);
            } else {
               return connectionA ? PowerNetwork.NetworkStrength.PowerPole : PowerNetwork.NetworkStrength.CopperCable;
            }
         }
      } else {
         return PowerNetwork.NetworkStrength.CopperCable;
      }
   }

   private PowerNetwork.NetworkStrength findConnection(Structure<?> a, Structure<?> b) {
      int keyA = a.getIndex();
      int keyB = b.getIndex();
      this.dfsScratch.add(keyA);
      IntMap<Boolean> connectionsA = (IntMap<Boolean>)this.highPowerConnections.get(keyA);
      Boolean connectionA = (Boolean)connectionsA.get(keyB);
      if (connectionA == null) {
         for (Edge e : (Edge[])this.edges.get(keyA)) {
            if (e != null && e.getNetworkStrength() == PowerNetwork.NetworkStrength.PowerPole) {
               Structure<?> edgeB = e.getOther(a);
               if (!this.dfsScratch.contains(edgeB.getIndex()) && edgeB.getIndex() == keyB) {
                  connectionA = true;
                  break;
               }
            }
         }

         if (connectionA == null) {
            for (Edge ex : (Edge[])this.edges.get(keyA)) {
               if (ex != null && ex.getNetworkStrength() == PowerNetwork.NetworkStrength.PowerPole) {
                  Structure<?> edgeB = ex.getOther(a);
                  if (!this.dfsScratch.contains(edgeB.getIndex())) {
                     connectionA = this.findConnection(edgeB, b) == PowerNetwork.NetworkStrength.PowerPole;
                     if (connectionA != null) {
                        break;
                     }
                  }
               }
            }
         }

         if (connectionA == null) {
            connectionA = false;
         }

         connectionsA.put(keyB, connectionA);
         IntMap<Boolean> connectionsB = (IntMap<Boolean>)this.highPowerConnections.get(keyB);
         connectionsB.put(keyA, connectionA);
      }

      return connectionA ? PowerNetwork.NetworkStrength.PowerPole : PowerNetwork.NetworkStrength.CopperCable;
   }

   public void clearHighPowerCache() {
      for (IntMap<Boolean> set : this.highPowerConnections.values()) {
         if (set != null) {
            set.clear();
         }
      }
   }

   public boolean isEmpty() {
      return this.allVertices.isEmpty();
   }

   public boolean hasEdges() {
      return !this.edgeSet.isEmpty();
   }

   private void manageConnectedness() {
      IntSet set = new IntSet();
      LinkedList<Integer> queue = new LinkedList<>();
      queue.add(((Entry)this.edges.iterator().next()).key);

      while (!queue.isEmpty()) {
         int n = queue.pop();
         Edge[] myEdges = (Edge[])this.edges.get(n);
         if (myEdges != null) {
            set.add(n);

            for (Edge e : myEdges) {
               if (e != null) {
                  if (e.getA().getIndex() == n && !set.contains(e.getB().getIndex())) {
                     queue.add(e.getB().getIndex());
                  } else if (e.getB().getIndex() == n && !set.contains(e.getA().getIndex())) {
                     queue.add(e.getA().getIndex());
                  }
               }
            }
         }
      }

      if (set.size < this.edges.size) {
         PowerNetwork net = new PowerNetwork(this.grid);
         Set<Structure<?>> verticesToRemove = new HashSet<>();

         for (Entry<Edge[]> o : this.edges.entries()) {
            if (!set.contains(o.key)) {
               for (Edge ex : (Edge[])o.value) {
                  if (ex != null) {
                     verticesToRemove.add(ex.getA());
                     verticesToRemove.add(ex.getB());
                     net.addEdge(ex);
                  }
               }
            }
         }

         for (Structure<?> s : verticesToRemove) {
            this.removeVertex(s, false);
         }

         this.grid.addNetwork(net);
      }
   }

   private void connect(
      Edge existingEdge, Structure<?> node, int nodeDockIndex, Structure<?> target, int targetDockIndex, PowerNetwork.NetworkStrength networkStrength
   ) {
      if (existingEdge == null) {
         PowerNetwork oldNetwork = target.getPowerNetwork();
         this.addEdge(node, nodeDockIndex, target, targetDockIndex, networkStrength);
         this.grid.mergeNetworks(node.getPowerNetwork(), oldNetwork);
      } else if (existingEdge.getA() == node && target != existingEdge.getB()) {
         existingEdge.setB(target);
         existingEdge.setDockA(nodeDockIndex);
         existingEdge.setDockB(targetDockIndex);
         this.addVertex(target);
      } else if (existingEdge.getB() == node && target != existingEdge.getA()) {
         existingEdge.setA(target);
         existingEdge.setDockA(targetDockIndex);
         existingEdge.setDockB(nodeDockIndex);
         this.addVertex(target);
      }
   }

   public void updateConnections(Structure<?> node) {
      int key = node.getIndex();
      Edge[] myEdges = (Edge[])this.edges.get(key);
      int i = 0;

      for (int j = 0; i < node.getDocks().length; i++) {
         Dock d = node.getDocks()[i];
         if (d.type == Dock.DockType.Power) {
            CopperCable c = node.layer.getCable(node.x + d.x + d.dir.dx, node.y + d.y + d.dir.dy);
            Edge e = myEdges[j];
            if (c != null) {
               CopperCable endA = c.walkToEnd();
               CopperCable endB = endA.walkToEnd();
               if ((endA == c || endB == c) && c.getStructures()[d.dir.inv().ordinal()] == node) {
                  CopperCable other = null;
                  if (endA == c) {
                     other = endB;
                  } else {
                     other = endA;
                  }

                  boolean any = false;

                  for (Structure<?> s : other.getStructures()) {
                     if (s != null && !(s instanceof CopperCable) && s != node) {
                        int k = 0;

                        for (Dock d1 : s.getDocks()) {
                           if (d1.type == Dock.DockType.Power && s.x + d1.x + d1.dir.dx == other.x && s.y + d1.y + d1.dir.dy == other.y) {
                              this.connect(e, node, j, s, k, PowerNetwork.NetworkStrength.CopperCable);
                              any = true;
                              break;
                           }

                           if (d1.type == Dock.DockType.Power || d1.type == Dock.DockType.BigPower) {
                              k++;
                           }
                        }

                        if (any) {
                           break;
                        }
                     }
                  }

                  if (!any && e != null) {
                     this.removeEdge(e, true);
                  }
               }
            } else if (e != null) {
               this.removeEdge(e, true);
            }

            j++;
         } else if (d.type == Dock.DockType.BigPower) {
            Structure<?> ghost = node.layer.getStructure(node.x + d.x + d.dir.dx, node.y + d.y + d.dir.dy);
            Edge e = myEdges[j];
            Structure<?> otherx = null;
            int otherDock = -1;
            if (ghost instanceof PowerPoleGhost) {
               PowerPole p = ((PowerPoleGhost)ghost).getPowerPole();
               PowerPole end = p.walkToEnd();
               if (end.getA() != null && !(end.getA() instanceof PowerPole) && end.getA() != node) {
                  otherx = end.getA();
                  otherDock = end.getAGhost().getDockIndex();
               } else if (end.getB() != null && !(end.getB() instanceof PowerPole) && end.getB() != node) {
                  otherx = end.getB();
                  otherDock = end.getBGhost().getDockIndex();
               }
            }

            if (otherx != null) {
               this.connect(e, node, j, otherx, otherDock, PowerNetwork.NetworkStrength.PowerPole);
            } else if (e != null) {
               this.removeEdge(e, true);
            }

            j++;
         }
      }
   }

   private boolean isArrayNull(Object[] o) {
      for (Object i : o) {
         if (i != null) {
            return false;
         }
      }

      return true;
   }

   private boolean removeEdge(Edge e, boolean createIslandNetworks) {
      int keyA = e.getA().getIndex();
      int keyB = e.getB().getIndex();
      Edge[] edgesOfA = (Edge[])this.edges.get(keyA);
      Edge[] edgesOfB = (Edge[])this.edges.get(keyB);
      if (edgesOfA != null && edgesOfB != null) {
         if (edgesOfA[e.getDockA()] == e && edgesOfB[e.getDockB()] == e) {
            this.edgeSet.remove(e);
            edgesOfA[e.getDockA()] = null;
            this.edges.put(keyA, edgesOfA);
            edgesOfB[e.getDockB()] = null;
            this.edges.put(keyB, edgesOfB);
            if (e.getNetworkStrength() == PowerNetwork.NetworkStrength.PowerPole) {
               IntMap<Boolean> connectionsA = (IntMap<Boolean>)this.highPowerConnections.get(keyA);
               connectionsA.remove(keyB);
               IntMap<Boolean> connectionsB = (IntMap<Boolean>)this.highPowerConnections.get(keyB);
               connectionsB.remove(keyA);
               this.grid.clearHighPowerCache();
            }

            if (createIslandNetworks) {
               if (this.hasVertex(e.getA()) && this.isArrayNull(edgesOfA)) {
                  PowerNetwork n = new PowerNetwork(this.grid);
                  this.removeVertex(e.getA(), true);
                  n.addVertex(e.getA());
                  this.grid.addNetwork(n);
               }

               if (this.hasVertex(e.getB()) && this.isArrayNull(edgesOfB)) {
                  PowerNetwork n = new PowerNetwork(this.grid);
                  this.removeVertex(e.getB(), true);
                  n.addVertex(e.getB());
                  this.grid.addNetwork(n);
               }
            }

            this.hadEdgeDeletion = true;
            this.minimumSpanningTreeDirty = true;
            return true;
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   public float getMeanInPerSecond() {
      return this.inTickMean.getMean() / this.timeTickMean.getMean();
   }

   public float getTimeTickMean() {
      return this.timeTickMean.getMean();
   }

   public float getMeanOutPerSecond() {
      return this.outTickMean.getMean() / this.timeTickMean.getMean();
   }

   public void addOutTick(double amount) {
      this.outTick += amount;
   }

   public OrderedSet<Structure<?>> getAllVertices() {
      return this.allVertices;
   }

   private boolean isValidMSTEdge(Structure<?> from, Edge e, HashSet<Structure<?>> includedVertices) {
      if (e == null) {
         return false;
      } else {
         Structure<?> other = e.getOther(from);
         if (other != null && !includedVertices.contains(other)) {
            return other.layer.getIndex() == from.layer.getIndex() ? true : from.x == other.x && from.y == other.y;
         } else {
            return false;
         }
      }
   }

   public Array<Edge> getMinimumSpanningTree() {
      if (this.minimumSpanningTreeDirty) {
         this.minimumSpanningTree.clear();
         if (this.allVertices.isEmpty()) {
            this.minimumSpanningTreeDirty = false;
            return this.minimumSpanningTree;
         }

         HashSet<Structure<?>> includedVertices = new HashSet<>();
         TreeSet<Edge> nextVertices = new TreeSet<>(new Comparator<Edge>() {
            public int compare(Edge o1, Edge o2) {
               int costA = Math.abs(o1.getB().getIndex() - o1.getA().getIndex());
               int costB = Math.abs(o2.getB().getIndex() - o2.getA().getIndex());
               return costA - costB == 0 ? o1.getA().getIndex() - o2.getA().getIndex() : costA - costB;
            }
         });
         Structure<?> start = (Structure<?>)this.allVertices.first();
         includedVertices.add(start);
         Edge[] startEdges = (Edge[])this.edges.get(start.getIndex());
         if (startEdges != null) {
            for (Edge e : startEdges) {
               if (this.isValidMSTEdge(start, e, includedVertices)) {
                  nextVertices.add(e);
               }
            }
         }

         int allVerts = this.allVertices.size;

         while (includedVertices.size() < allVerts && !nextVertices.isEmpty()) {
            if (!nextVertices.isEmpty()) {
               Edge ex = nextVertices.pollFirst();
               boolean hasA = includedVertices.contains(ex.getA());
               boolean hasB = includedVertices.contains(ex.getB());
               if (!hasA || !hasB) {
                  this.minimumSpanningTree.add(ex);
                  Structure<?> newVert = ex.getA();
                  if (hasA) {
                     newVert = ex.getB();
                  }

                  includedVertices.add(newVert);
                  Edge[] nextEdges = (Edge[])this.edges.get(newVert.getIndex());
                  if (nextEdges != null) {
                     for (Edge edge : nextEdges) {
                        if (this.isValidMSTEdge(newVert, edge, includedVertices)) {
                           nextVertices.add(edge);
                        }
                     }
                  }
               }
            }
         }

         this.minimumSpanningTreeDirty = false;
      }

      return this.minimumSpanningTree;
   }

   @Override
   public String toString() {
      String s = "{";
      OrderedSetIterator var2 = this.donorVertices.iterator();

      while (var2.hasNext()) {
         Structure<?> p = (Structure<?>)var2.next();
         s = s + p.getIndex() + ", ";
      }

      if (s.length() > 1) {
         s = s.substring(0, s.length() - 2);
      }

      s = s + "; ";
      var2 = this.receiverVertices.iterator();

      while (var2.hasNext()) {
         Structure<?> p = (Structure<?>)var2.next();
         s = s + p.getIndex() + ", ";
      }

      if (s.length() > 1) {
         s = s.substring(0, s.length() - 2);
      }

      s = s + "; ";
      var2 = this.neutralVertices.iterator();

      while (var2.hasNext()) {
         Structure<?> p = (Structure<?>)var2.next();
         s = s + p.getIndex() + ", ";
      }

      if (s.length() > 1) {
         s = s.substring(0, s.length() - 2);
      }

      return s + "}";
   }

   public static enum NetworkStrength {
      CopperCable(450000.0),
      PowerPole(1.0E9);

      public final double maxPowerPerSecond;

      private NetworkStrength(double pps) {
         this.maxPowerPerSecond = pps;
      }
   }
}
