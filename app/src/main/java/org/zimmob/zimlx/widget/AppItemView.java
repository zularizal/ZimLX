package org.zimmob.zimlx.widget;


import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.HapticFeedbackConstants;
import android.view.View;

import org.zimmob.zimlx.R;
import org.zimmob.zimlx.activity.HomeActivity;
import org.zimmob.zimlx.config.Config;
import org.zimmob.zimlx.manager.Setup;
import org.zimmob.zimlx.model.App;
import org.zimmob.zimlx.model.Item;
import org.zimmob.zimlx.dragndrop.DragAction;
import org.zimmob.zimlx.dragndrop.DragHandler;
import org.zimmob.zimlx.util.Tool;
import org.zimmob.zimlx.viewutil.GroupIconDrawable;
import org.zimmob.zimlx.viewutil.IDesktopCallback;
import org.zimmob.zimlx.viewutil.ItemGestureListener;

import java.util.Objects;

public class AppItemView extends View implements Drawable.Callback {

    private static final int MIN_ICON_TEXT_MARGIN = 8;
    private static final char ELLIPSIS = '…';

    private Drawable icon = null;
    private String _label;
    private Paint _textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Rect _textContainer = new Rect(), testTextContainer = new Rect();
    private Typeface _typeface;
    private float _iconSize;
    private boolean _showLabel = true;
    private boolean _vibrateWhenLongPress;
    private float _labelHeight;
    private int _targetedWidth;
    private int _targetedHeightPadding;
    private float _heightPadding;

    public AppItemView(Context context) {
        this(context, null);
    }

    public AppItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (_typeface == null) {
            _typeface = Typeface.createFromAsset(getContext().getAssets(), "RobotoCondensed-Regular.ttf");
        }
        setWillNotDraw(false);
        setDrawingCacheEnabled(true);
        setWillNotCacheDrawing(false);

        _labelHeight = Tool.dp2px(14, getContext());
        _textPaint.setTextSize(Tool.sp2px(getContext(), 13));
        _textPaint.setColor(Color.DKGRAY);
        _textPaint.setTypeface(_typeface);
    }

    public static AppItemView createAppItemViewPopup(Context context, Item groupItem, int iconSize, float fontSizeSp) {
        AppItemView.Builder b = new AppItemView.Builder(context, iconSize)
                .withOnTouchGetPosition(groupItem, Setup.itemGestureCallback())
                .setTextColor(Setup.appSettings().getFolderLabelColor())
                .setFontSize(context, fontSizeSp);
        if (groupItem.getType() == Item.Type.SHORTCUT) {
            b.setShortcutItem(groupItem);
        }
        else {
            App app = Setup.appLoader().findItemApp(groupItem);
            if (app != null) {
                b.setAppItem(groupItem, app);
            }
        }
        return b.getView();
    }

    public static View createDrawerAppItemView(Context context, App app, int iconSize, AppItemView.LongPressCallBack longPressCallBack) {
        return new AppItemView.Builder(context, iconSize)
                .setAppItem(app)
                .withOnTouchGetPosition(null, null)
                .withOnLongClick(app, DragAction.Action.APP_DRAWER, longPressCallBack)
                .setLabelVisibility(Setup.appSettings().isDrawerShowLabel())
                .setTextColor(Setup.appSettings().getDrawerLabelColor())
                .setFontSize(context, Setup.appSettings().getDrawerLabelFontSize())
                .getView();
    }

    @Override
    public Bitmap getDrawingCache() {
        return Tool.drawableToBitmap(icon);
    }

    public Drawable getCurrentIcon() {
        return this.icon;
    }

    public void setCurrentIcon(Drawable icon) {
        this.icon = icon;
    }

    public View getView() {
        return this;
    }

    public String getLabel() {
        return _label;
    }

    public void setLabel(String label) {
        this._label = label;
    }

    public float getIconSize() {
        return _iconSize;
    }

    private void setIconSize(float iconSize) {
        this._iconSize = iconSize;
    }

    public boolean getShowLabel() {
        return _showLabel;
    }

    public void setTargetedWidth(int width) {
        _targetedWidth = width;
    }

    public void setTargetedHeightPadding(int padding) {
        _targetedHeightPadding = padding;
    }


    public void reset() {
        _label = "";
        this.icon = null;
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        float mWidth = _iconSize;
        float mHeight = _iconSize + (_showLabel ? 0 : _labelHeight);
        if (_targetedWidth != 0) {
            mWidth = _targetedWidth;
        }
        setMeasuredDimension((int) Math.ceil(mWidth), (int) Math.ceil((int) mHeight) + Tool.dp2px(2, getContext()) + _targetedHeightPadding * 2);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        _heightPadding = (getHeight() - _iconSize - (_showLabel ? _labelHeight : 0)) / 2f;
        if (_label != null && _showLabel) {
            _textPaint.getTextBounds(_label, 0, _label.length(), _textContainer);
            int maxTextWidth = getWidth() - MIN_ICON_TEXT_MARGIN * 2;

            // use ellipsis if the label is too long
            if (_textContainer.width() > maxTextWidth) {
                String testLabel = _label + ELLIPSIS;
                _textPaint.getTextBounds(testLabel, 0, testLabel.length(), testTextContainer);

                //Premeditate to be faster
                float characterSize = testTextContainer.width() / testLabel.length();
                int charsToTruncate = (int) ((testTextContainer.width() - maxTextWidth) / characterSize);

                canvas.drawText(_label.substring(0, _label.length() - charsToTruncate) + ELLIPSIS,
                        MIN_ICON_TEXT_MARGIN, getHeight() - _heightPadding, _textPaint);
            }
            else {
                canvas.drawText(_label, (getWidth() - _textContainer.width()) / 2f, getHeight() - _heightPadding, _textPaint);
            }
        }

        // center the icon
        if (icon != null) {
            canvas.save();
            canvas.translate((getWidth() - _iconSize) / 2, _heightPadding);
            icon.setBounds(0, 0, (int) _iconSize, (int) _iconSize);
            icon.draw(canvas);
            canvas.restore();
        }
    }

    public float getDrawIconTop() {
        return _heightPadding;
    }

    public float getDrawIconLeft() {
        return (getWidth() - _iconSize) / 2;
    }

    public interface LongPressCallBack {
        boolean readyForDrag(View view);

        void afterDrag(View view);
    }

    public static class Builder {
        AppItemView _view;

        public Builder(Context context, int iconSize) {
            _view = new AppItemView(context);
            _view.setIconSize(Tool.dp2px(iconSize, _view.getContext()));
        }

        public Builder(AppItemView view, int iconSize) {
            this._view = view;
            view.setIconSize(Tool.dp2px(iconSize, view.getContext()));
        }

        public static OnLongClickListener getLongClickDragAppListener(final Item item, final DragAction.Action action, @Nullable final LongPressCallBack eventAction) {
            return v -> {
                if (Setup.appSettings().isDesktopLock()) {
                    return false;
                }
                if (eventAction != null && eventAction.readyForDrag(v)) {
                    return false;
                }
                v.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
                DragHandler.startDrag(v, item, action, eventAction);
                return true;
            };
        }

        public AppItemView getView() {
            return _view;
        }

        Builder setAppItem(final App app) {
            _view.setLabel(app.getLabel());
            _view.setCurrentIcon(app.getIcon());
            _view.setOnClickListener(v -> Tool.createScaleInScaleOutAnim(_view, () -> Tool.startApp(_view.getContext(), app, _view), 0.85f));
            return this;
        }

        public Builder setAppItem(final Item item, final App app) {
            _view.setLabel(item.getLabel());
            _view.setCurrentIcon(app.getIcon());
            _view.setOnClickListener(v -> Tool.createScaleInScaleOutAnim(_view, () -> Tool.startApp(_view.getContext(), app, _view), 0.85f));
            return this;
        }

        public Builder setShortcutItem(final Item item) {
            _view.setLabel(item.getLabel());
            _view.setCurrentIcon(item.getIcon());
            _view.setOnClickListener(v -> Tool.createScaleInScaleOutAnim(_view, () -> _view.getContext().startActivity(item.getIntent()), 0.85f));
            return this;
        }

        public Builder setGroupItem(Context context, final IDesktopCallback callback, final Item item, int iconSize) {
            _view.setLabel(item.getLabel());
            _view.setCurrentIcon(new GroupIconDrawable(context, item, iconSize));
            _view.setOnClickListener(v -> {
                if (HomeActivity.Companion.getLauncher() != null && (HomeActivity.Companion.getLauncher()).getGroupPopup().showWindowV(item, v, callback)) {
                    ((GroupIconDrawable) ((AppItemView) v).getCurrentIcon()).popUp();
                }
            });
            return this;
        }

        public Builder setActionItem(Item item) {
            _view.setLabel(item.getLabel());
            _view.setCurrentIcon(ContextCompat.getDrawable(Setup.appContext(), R.drawable.ic_apps_white_48dp));
            switch (item.getActionValue()) {
                case Config.ACTION_LAUNCHER:
                    _view.setOnClickListener(view -> {
                        view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
                        if (HomeActivity.Companion.getLauncher() != null) {
                            Objects.requireNonNull(HomeActivity.Companion.getLauncher()).openAppDrawer(view);
                        }
                    });
                    break;
            }
            return this;
        }

        private Builder withOnLongClick(final App app, final DragAction.Action action, @Nullable final LongPressCallBack eventAction) {
            withOnLongClick(Item.newAppItem(app), action, eventAction);
            return this;
        }

        public Builder withOnLongClick(final Item item, final DragAction.Action action, @Nullable final LongPressCallBack eventAction) {
            _view.setOnLongClickListener(v -> {
                if (Setup.appSettings().isDesktopLock()) {
                    return false;
                }
                if (eventAction != null && eventAction.readyForDrag(v)) {
                    return false;
                }
                if (_view._vibrateWhenLongPress) {
                    v.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
                }
                DragHandler.startDrag(_view, item, action, eventAction);
                return true;
            });
            return this;
        }

        @SuppressLint("ClickableViewAccessibility")
        public Builder withOnTouchGetPosition(Item item, ItemGestureListener.ItemGestureCallback itemGestureCallback) {
            _view.setOnTouchListener(Tool.getItemOnTouchListener(item, itemGestureCallback));
            return this;
        }

        public Builder setTextColor(@ColorInt int color) {
            _view._textPaint.setColor(color);
            return this;
        }

        public Builder setFontSize(Context context, float fontSizeSp) {
            _view._textPaint.setTextSize(Tool.sp2px(context, fontSizeSp));
            return this;
        }

        public Builder setLabelVisibility(boolean visible) {
            _view._showLabel = visible;
            return this;
        }

        public Builder vibrateWhenLongPress() {
            _view._vibrateWhenLongPress = true;
            return this;
        }

        public Builder setFastAdapterItem() {
            return this;
        }
    }
}
