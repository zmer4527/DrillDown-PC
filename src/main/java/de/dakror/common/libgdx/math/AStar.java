package de.dakror.common.libgdx.math;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

public class AStar<T> {
   LinkedList<AStar<T>.Node> openList = new LinkedList<>();
   HashSet<AStar<T>.Node> closedList = new HashSet<>();
   AStar.Network<T> network;
   T finish;
   Comparator<AStar<T>.Node> comparator = new Comparator<AStar<T>.Node>() {
      public int compare(AStar<T>.Node a, AStar<T>.Node b) {
         return Float.compare(a.g + a.h, b.g + b.h);
      }
   };
   long maxTime;

   private void neighborVisitor(AStar<T>.Node parent, T n) {
      AStar<T>.Node node = new AStar.Node(n, parent);
      if (!this.closedList.contains(node)) {
         int index = this.openList.indexOf(node);
         if (index > -1) {
            AStar<T>.Node old = this.openList.get(index);
            if (old.g > node.g) {
               old.setParent(parent);
            }
         } else {
            node.h = this.network.getH(n, this.finish);
            this.openList.add(node);
         }
      }
   }

   public void setMaxTime(long maxTime) {
      this.maxTime = maxTime;
   }

   public long getMaxTime() {
      return this.maxTime;
   }

   public LinkedList<T> findPath(AStar.Network<T> network, T start, T finish) {
      this.network = network;
      LinkedList<T> path = new LinkedList<>();
      if (start == finish) {
         path.add(start);
         return path;
      } else {
         this.finish = finish;
         this.openList.clear();
         this.closedList.clear();
         AStar<T>.Node startNode = new AStar.Node(start, null);
         startNode.h = network.getH(start, finish);
         this.openList.add(startNode);
         long now = System.nanoTime();

         while (!this.openList.isEmpty() && (this.maxTime <= 0L || System.nanoTime() - now <= this.maxTime * 1000000L)) {
            Collections.sort(this.openList, this.comparator);
            final AStar<T>.Node n = this.openList.poll();
            if (n.data.equals(finish)) {
               for (AStar<T>.Node t = n; t != null; t = t.parent) {
                  path.add(t.data);
               }

               Collections.reverse(path);
               return path;
            }

            this.closedList.add(n);
            network.visitNeighbors(n.data, start, finish, new AStar.Visitor<T>() {
               @Override
               public void visit(T x) {
                  AStar.this.neighborVisitor(n, x);
               }
            });
         }

         return path;
      }
   }

   public List<T> getOpenList() {
      List<T> l = new ArrayList<>();

      for (AStar<T>.Node n : this.openList) {
         l.add(n.data);
      }

      return l;
   }

   public List<T> getClosedList() {
      List<T> l = new ArrayList<>();

      for (AStar<T>.Node n : this.closedList) {
         l.add(n.data);
      }

      return l;
   }

   public abstract static class Network<T> {
      public abstract float getH(T var1, T var2);

      public abstract float getEdgeLength(T var1, T var2);

      public abstract void visitNeighbors(T var1, T var2, T var3, AStar.Visitor<T> var4);
   }

   private class Node {
      private AStar<T>.Node parent;
      private float g;
      private float h;
      private T data;

      Node(T data, AStar<T>.Node parent) {
         this.data = data;
         this.setParent(parent);
      }

      void setParent(AStar<T>.Node parent) {
         this.parent = parent;
         if (parent == null) {
            this.g = 0.0F;
         } else {
            this.g = parent.g + AStar.this.network.getEdgeLength(parent.data, this.data);
         }
      }

      @Override
      public int hashCode() {
         return this.data.hashCode();
      }

      @Override
      public boolean equals(Object obj) {
         return obj.getClass().equals(this.getClass()) && this.hashCode() == obj.hashCode();
      }

      @Override
      public String toString() {
         return String.format("{%s, g=%f, h=%f}", this.data.toString(), this.g, this.h);
      }
   }

   public interface Visitor<T> {
      void visit(T var1);
   }
}
