package org.zimmob.zimlx.viewutil;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;

import org.zimmob.zimlx.manager.Setup;
import org.zimmob.zimlx.model.App;
import org.zimmob.zimlx.model.Item;
import org.zimmob.zimlx.util.AppSettings;
import org.zimmob.zimlx.util.Tool;

import static org.zimmob.zimlx.config.Config.FOLDER_SHAPE_CIRCLE;
import static org.zimmob.zimlx.config.Config.FOLDER_SHAPE_CIRCLE_SHADOW;
import static org.zimmob.zimlx.config.Config.FOLDER_SHAPE_SQUARE;
import static org.zimmob.zimlx.config.Config.FOLDER_SHAPE_SQUARE_SHADOW;

public class GroupIconDrawable extends Drawable {
    private Drawable[] icons;

    private Paint paintIcon;
    private boolean needAnimate;
    private boolean needAnimateScale;
    private float scaleFactor = 1;
    private float iconSize;
    private float padding;
    private int outline;
    private int iconSizeDiv2;
    private AppSettings appSettings = Setup.appSettings();

    public GroupIconDrawable(Context context, Item item, int iconSize) {
        final float size = Tool.dp2px(iconSize, context);
        final Drawable[] icons = new Drawable[4];
        for (int i = 0; i < 4; i++) {
            icons[i] = null;
        }
        init(icons, size);
        for (int i = 0; i < 4 && i < item.getItems().size(); i++) {
            Item temp = item.getItems().get(i);
            App app = null;
            if (temp != null) {
                app = Setup.appLoader().findItemApp(temp);
            }
            if (app == null) {
                Setup.logger().log(this, Log.DEBUG, null, "Item %s has a null app at index %d (Intent: %s)", item.getLabel(), i, temp == null ? "Item is NULL" : temp.getIntent());
                icons[i] = new ColorDrawable(Color.TRANSPARENT);
            } else {
                icons[i] = app.getIcon();
            }
        }
    }

    @Override
    public int getIntrinsicHeight() {
        return (int) iconSize;
    }

    @Override
    public int getIntrinsicWidth() {
        return (int) iconSize;
    }

    private void init(Drawable[] icons, float size) {
        this.icons = icons;
        this.iconSize = size;
        iconSizeDiv2 = Math.round(iconSize / 2f);
        padding = iconSize / 20f;

        this.paintIcon = new Paint();
        paintIcon.setAntiAlias(true);
        paintIcon.setFilterBitmap(true);
    }

    public void popUp() {
        needAnimate = true;
        needAnimateScale = true;
        invalidateSelf();
    }

    public void popBack() {
        needAnimate = false;
        needAnimateScale = false;
        invalidateSelf();
    }

    @Override
    public void draw(Canvas canvas) {
        canvas.save();
        Paint paintBackground = new Paint();
        if (needAnimateScale)
            scaleFactor = Tool.clampFloat(scaleFactor - 0.09f, 0.4f, 1f);
        else
            scaleFactor = Tool.clampFloat(scaleFactor + 0.09f, 0.4f, 1f);

        paintBackground.setAlpha(150);
        paintBackground.setAntiAlias(true);
        canvas.scale(scaleFactor, scaleFactor, iconSize / 2, iconSize / 2);
        int mode = appSettings.getFolderShape();
        Path clip = new Path();
        Rect rect;
        switch (mode) {
            case FOLDER_SHAPE_CIRCLE:
                paintBackground.setColor(Color.parseColor("#A4101947"));

                //create circle
                clip.addCircle(iconSize / 2, iconSize / 2, iconSize / 2 - outline, Path.Direction.CW);
                canvas.clipPath(clip, Region.Op.REPLACE);
                canvas.drawCircle(iconSize / 2, iconSize / 2, iconSize / 2 - outline, paintBackground);
                break;

            case FOLDER_SHAPE_CIRCLE_SHADOW:
                paintBackground.setColor(Color.parseColor("#CFC6C6C6"));
                //create circle
                clip.addCircle(iconSize / 2 + 2, iconSize / 2 + 2, iconSize / 2 - outline, Path.Direction.CW);
                canvas.clipPath(clip, Region.Op.REPLACE);
                canvas.drawCircle(iconSize / 2, iconSize / 2, iconSize / 2 - outline, paintBackground);
                break;

            case FOLDER_SHAPE_SQUARE:
                paintBackground.setColor(Color.parseColor("#A4101947"));

                rect = new Rect(0, 0, (int) iconSize, (int) iconSize);
                RectF rectF = new RectF(rect);
                canvas.drawRoundRect(rectF, 20, 20, paintBackground);
                break;

            case FOLDER_SHAPE_SQUARE_SHADOW:
                paintBackground.setColor(Color.parseColor("#CFC6C6C6"));
                rect = new Rect(0, 0, (int) iconSize, (int) iconSize);
                rectF = new RectF(rect);
                canvas.drawRoundRect(rectF, 20, 20, paintBackground);
                break;
        }

        if (icons[0] != null)
            drawIcon(canvas, icons[0], padding, padding, iconSizeDiv2 - padding, iconSizeDiv2 - padding, paintIcon);
        if (icons[1] != null)
            drawIcon(canvas, icons[1], iconSizeDiv2 + padding, padding, iconSize - padding, iconSizeDiv2 - padding, paintIcon);
        if (icons[2] != null)
            drawIcon(canvas, icons[2], padding, iconSizeDiv2 + padding, iconSizeDiv2 - padding, iconSize - padding, paintIcon);
        if (icons[3] != null)
            drawIcon(canvas, icons[3], iconSizeDiv2 + padding, iconSizeDiv2 + padding, iconSize - padding, iconSize - padding, paintIcon);
        canvas.restore();

        if (needAnimate) {
            paintIcon.setAlpha(Tool.clampInt(paintIcon.getAlpha() - 25, 0, 255));
            invalidateSelf();
        } else if (paintIcon.getAlpha() != 255) {
            paintIcon.setAlpha(Tool.clampInt(paintIcon.getAlpha() + 25, 0, 255));
            invalidateSelf();
        }
    }

    /**
     * @param canvas
     * @param icon
     * @param l
     * @param t
     * @param r
     * @param b
     * @param paint
     */
    private void drawIcon(Canvas canvas, Drawable icon, float l, float t, float r, float b, Paint paint) {
        icon.setBounds((int) l, (int) t, (int) r, (int) b);
        icon.setFilterBitmap(true);
        icon.setAlpha(paint.getAlpha());
        icon.draw(canvas);
    }

    @Override
    public void setAlpha(int i) {
    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSPARENT;
    }

}
