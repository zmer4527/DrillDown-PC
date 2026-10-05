package de.dakror.quarry.ui;

public final class UiLayoutMetrics {
   public static final int LEFT = 0;
   public static final int TOP = 1;
   public static final int RIGHT = 2;
   public static final int BOTTOM = 3;
   private static final float COMPACT_WIDTH_LIMIT = 600.0F;
   private static final float WIDE_WIDTH_LIMIT = 1000.0F;
   private static final float ULTRAWIDE_ASPECT_RATIO = 2.0F;
   private final int[] safeInsets = new int[4];
   private int screenWidth;
   private int screenHeight;
   private int viewportX;
   private int viewportY;
   private int viewportWidth;
   private int viewportHeight;
   private float uiWidth;
   private float uiHeight;
   private float contentLeft;
   private float contentBottom;
   private float contentRight;
   private float contentTop;
   private float aspectRatio;
   private UiLayoutMetrics.Orientation orientation = UiLayoutMetrics.Orientation.Square;
   private UiLayoutMetrics.WidthClass widthClass = UiLayoutMetrics.WidthClass.Standard;

   public boolean update(float uiW, float uiH, int screenW, int screenH, int[] insets) {
      int prevScreenWidth = this.screenWidth;
      int prevScreenHeight = this.screenHeight;
      int prevViewportX = this.viewportX;
      int prevViewportY = this.viewportY;
      int prevViewportWidth = this.viewportWidth;
      int prevViewportHeight = this.viewportHeight;
      float prevUiWidth = this.uiWidth;
      float prevUiHeight = this.uiHeight;
      int prevSafeLeft = this.safeInsets[0];
      int prevSafeTop = this.safeInsets[1];
      int prevSafeRight = this.safeInsets[2];
      int prevSafeBottom = this.safeInsets[3];
      this.uiWidth = Math.max(0.0F, uiW);
      this.uiHeight = Math.max(0.0F, uiH);
      this.screenWidth = Math.max(0, screenW);
      this.screenHeight = Math.max(0, screenH);
      if (this.uiWidth > 0.0F && this.uiHeight > 0.0F) {
         float ratio = this.uiWidth / this.uiHeight;
         this.viewportWidth = (int)((float)this.screenHeight * ratio);
         this.viewportHeight = this.screenHeight;
         if (this.viewportWidth > this.screenWidth) {
            this.viewportWidth = this.screenWidth;
            this.viewportHeight = (int)((float)this.screenWidth / ratio);
         }

         this.viewportX = (this.screenWidth - this.viewportWidth) / 2;
         this.viewportY = (this.screenHeight - this.viewportHeight) / 2;
      } else {
         this.viewportX = 0;
         this.viewportY = 0;
         this.viewportWidth = this.screenWidth;
         this.viewportHeight = this.screenHeight;
      }

      if (insets != null) {
         for (int i = 0; i < 4; i++) {
            this.safeInsets[i] = Math.max(0, insets[i]);
         }
      } else {
         for (int i = 0; i < 4; i++) {
            this.safeInsets[i] = 0;
         }
      }

      this.updateInsets();
      this.calculateContentArea();
      return prevScreenWidth != this.screenWidth
         || prevScreenHeight != this.screenHeight
         || prevViewportX != this.viewportX
         || prevViewportY != this.viewportY
         || prevViewportWidth != this.viewportWidth
         || prevViewportHeight != this.viewportHeight
         || prevUiWidth != this.uiWidth
         || prevUiHeight != this.uiHeight
         || prevSafeLeft != this.safeInsets[0]
         || prevSafeTop != this.safeInsets[1]
         || prevSafeRight != this.safeInsets[2]
         || prevSafeBottom != this.safeInsets[3];
   }

   private void updateInsets() {
      int totalHorizontal = this.safeInsets[0] + this.safeInsets[2];
      int totalVertical = this.safeInsets[1] + this.safeInsets[3];
      if (totalHorizontal > this.viewportWidth || totalVertical > this.viewportHeight) {
         if (totalHorizontal > this.viewportWidth) {
            float scale = (float)this.viewportWidth / (float)totalHorizontal;
            this.safeInsets[0] = (int)((float)this.safeInsets[0] * scale);
            this.safeInsets[2] = (int)((float)this.safeInsets[2] * scale);
         }

         if (totalVertical > this.viewportHeight) {
            float scale = (float)this.viewportHeight / (float)totalVertical;
            this.safeInsets[1] = (int)((float)this.safeInsets[1] * scale);
            this.safeInsets[3] = (int)((float)this.safeInsets[3] * scale);
         }
      }

      this.safeInsets[0] = Math.max(0, Math.min(this.safeInsets[0], this.viewportWidth / 2));
      this.safeInsets[2] = Math.max(0, Math.min(this.safeInsets[2], this.viewportWidth / 2));
      this.safeInsets[1] = Math.max(0, Math.min(this.safeInsets[1], this.viewportHeight / 2));
      this.safeInsets[3] = Math.max(0, Math.min(this.safeInsets[3], this.viewportHeight / 2));
   }

   private void calculateContentArea() {
      this.contentLeft = (float)this.safeInsets[0];
      this.contentBottom = (float)this.safeInsets[3];
      this.contentRight = Math.max(this.contentLeft, this.uiWidth - (float)this.safeInsets[2]);
      this.contentTop = Math.max(this.contentBottom, this.uiHeight - (float)this.safeInsets[1]);
      if (this.uiWidth > this.uiHeight) {
         this.orientation = UiLayoutMetrics.Orientation.Landscape;
      } else if (this.uiHeight > this.uiWidth) {
         this.orientation = UiLayoutMetrics.Orientation.Portrait;
      } else {
         this.orientation = UiLayoutMetrics.Orientation.Square;
      }

      this.aspectRatio = this.uiWidth > 0.0F && this.uiHeight > 0.0F ? Math.max(this.uiWidth, this.uiHeight) / Math.min(this.uiWidth, this.uiHeight) : 1.0F;
      if (this.uiWidth < 600.0F) {
         this.widthClass = UiLayoutMetrics.WidthClass.Standard;
      } else if (this.uiWidth < 1000.0F) {
         this.widthClass = UiLayoutMetrics.WidthClass.Wide;
      } else {
         this.widthClass = UiLayoutMetrics.WidthClass.UltraWide;
      }
   }

   public int getScreenWidth() {
      return this.screenWidth;
   }

   public int getScreenHeight() {
      return this.screenHeight;
   }

   public int getViewportX() {
      return this.viewportX;
   }

   public int getViewportY() {
      return this.viewportY;
   }

   public int getViewportWidth() {
      return this.viewportWidth;
   }

   public int getViewportHeight() {
      return this.viewportHeight;
   }

   public float getUiWidth() {
      return this.uiWidth;
   }

   public float getUiHeight() {
      return this.uiHeight;
   }

   public float getContentLeft() {
      return this.contentLeft;
   }

   public float getContentBottom() {
      return this.contentBottom;
   }

   public float getContentRight() {
      return this.contentRight;
   }

   public float getContentTop() {
      return this.contentTop;
   }

   public float getAspectRatio() {
      return this.aspectRatio;
   }

   public UiLayoutMetrics.Orientation getOrientation() {
      return this.orientation;
   }

   public UiLayoutMetrics.WidthClass getWidthClass() {
      return this.widthClass;
   }

   public int[] getSafeInsets() {
      return this.safeInsets;
   }

   public boolean isPortrait() {
      return this.orientation == UiLayoutMetrics.Orientation.Portrait;
   }

   public boolean isLandscape() {
      return this.orientation == UiLayoutMetrics.Orientation.Landscape;
   }

   public boolean isWide() {
      return this.widthClass == UiLayoutMetrics.WidthClass.Wide;
   }

   public boolean isUltraWide() {
      return this.isLandscape() && this.aspectRatio >= 2.0F;
   }

   public float getStableContentLeft() {
      return this.isLandscape() ? this.getBalancedHorizontalInset() : this.contentLeft;
   }

   public float getStableContentRight() {
      return this.isLandscape() ? this.getStableContentLeft() + this.uiWidth - this.getBalancedHorizontalInset() : this.contentRight;
   }

   public float getStableContentWidth() {
      return Math.max(0.0F, this.getStableContentRight() - this.getStableContentLeft());
   }

   private float getBalancedHorizontalInset() {
      return (float)Math.max(this.safeInsets[0], this.safeInsets[2]);
   }

   @Override
   public String toString() {
      return "UiLayoutMetrics{screen="
         + this.screenWidth
         + "x"
         + this.screenHeight
         + ", viewport="
         + this.viewportX
         + ","
         + this.viewportY
         + ","
         + this.viewportWidth
         + "x"
         + this.viewportHeight
         + ", ui="
         + this.uiWidth
         + "x"
         + this.uiHeight
         + ", insets="
         + this.safeInsets[0]
         + ","
         + this.safeInsets[1]
         + ","
         + this.safeInsets[2]
         + ","
         + this.safeInsets[3]
         + ", orientation="
         + this.orientation
         + ", widthClass="
         + this.widthClass
         + "}";
   }

   public static enum Orientation {
      Portrait,
      Landscape,
      Square;
   }

   public static enum WidthClass {
      Standard,
      Wide,
      UltraWide;
   }
}
