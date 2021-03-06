/**
 *  Paintroid: An image manipulation application for Android.
 *  Copyright (C) 2010-2015 The Catrobat Team
 *  (<http://developer.catrobat.org/credits>)
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.catrobat.paintroid.tools.implementation;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Typeface;
import android.os.Build;
import android.support.v4.app.FragmentManager;

import org.catrobat.paintroid.MainActivity;
import org.catrobat.paintroid.PaintroidApplication;
import org.catrobat.paintroid.R;
import org.catrobat.paintroid.command.Command;
import org.catrobat.paintroid.command.implementation.TextToolCommand;
import org.catrobat.paintroid.dialog.IndeterminateProgressDialog;
import org.catrobat.paintroid.dialog.TextToolDialog;
import org.catrobat.paintroid.dialog.colorpicker.ColorPickerDialog;
import org.catrobat.paintroid.tools.ToolType;
import org.catrobat.paintroid.ui.DrawingSurface;
import org.catrobat.paintroid.ui.TopBar;


public class TextTool extends BaseToolWithRectangleShape {

	private static final boolean ROTATION_ENABLED = true;
	private static final boolean RESPECT_IMAGE_BORDERS = false;
	private static final boolean RESIZE_POINTS_VISIBLE = true;

	private TextToolDialog.OnTextToolDialogChangedListener mOnTextToolDialogChangedListener;
	private ColorPickerDialog.OnColorPickedListener mOnColorPickedListener;
	private String mText = "";
	private String[] mMultilineText = {""};
	private String mFont = "Monospace";
	private boolean mUnderlined = false;
	private boolean mItalic = false;
	private boolean mBold = false;
	private int mTextSize = 20;
	private int mTextSizeMagnificationFactor = 3;
	private int mBoxOffset = 20;
	private float mMarginTop = 50.0f;
	private Paint mTextPaint;


	public TextTool(Context context, ToolType toolType) {
		super(context, toolType);

		setRotationEnabled(ROTATION_ENABLED);
		setRespectImageBounds(RESPECT_IMAGE_BORDERS);
		setResizePointsVisible(RESIZE_POINTS_VISIBLE);

		initializePaint();
		TextToolDialog.getInstance().setDefaultDialogValues();
		setupOnTextToolDialogChangedListener();
		mOnColorPickedListener = new ColorPickerDialog.OnColorPickedListener() {
			@Override
			public void colorChanged(int color) {
				changeTextColor();
			}
		};
		ColorPickerDialog.getInstance().addOnColorPickedListener(mOnColorPickedListener);

		createAndSetBitmap();
		resetBoxPosition();
		showTextToolDialog();
	}

	public void initializePaint() {
		mTextPaint = new Paint();
		mTextPaint.setAntiAlias(DEFAULT_ANTIALISING_ON);

		mTextPaint.setColor(mCanvasPaint.getColor());
		mTextPaint.setTextSize(mTextSize*mTextSizeMagnificationFactor);
		mTextPaint.setUnderlineText(mUnderlined);
		mTextPaint.setFakeBoldText(mBold);

		updateTypeface();
	}

	public void createAndSetBitmap() {
		float textDescent = mTextPaint.descent();
		float textAscent = mTextPaint.ascent();

		float upperBoxEdge = mToolPosition.y - mBoxHeight/2.0f;
		float textHeight = textDescent - textAscent;
		mBoxHeight = textHeight * mMultilineText.length + 2*mBoxOffset;
		mToolPosition.y = upperBoxEdge + mBoxHeight/2.0f;

		float maxTextWidth = 0;
		for (String str : mMultilineText) {
			float textWidth = mTextPaint.measureText(str);
			if (textWidth > maxTextWidth) {
				maxTextWidth = textWidth;
			}
		}
		mBoxWidth = maxTextWidth + 2*mBoxOffset;

		Bitmap bitmap = Bitmap.createBitmap((int) mBoxWidth, (int) mBoxHeight,
				Bitmap.Config.ARGB_8888);
		Canvas drawCanvas = new Canvas(bitmap);

		for (int i = 0; i < mMultilineText.length; i++) {
			drawCanvas.drawText(mMultilineText[i], mBoxOffset, mBoxOffset - textAscent + textHeight*i, mTextPaint);
		}

		mDrawingBitmap = bitmap;
	}

	protected void showTextToolDialog() {
		FragmentManager fm = ((MainActivity) mContext).getSupportFragmentManager();
		TextToolDialog.getInstance().show(fm, "texttool");
	}

	protected void setupOnTextToolDialogChangedListener() {
		mOnTextToolDialogChangedListener = new TextToolDialog.OnTextToolDialogChangedListener() {
			@Override
			public void setText(String text) {
				mText = text;
				mMultilineText = mText.split("\n");
				createAndSetBitmap();
			}

			@Override
			public void setFont(String font) {
				mFont = font;
				updateTypeface();
				createAndSetBitmap();
			}

			@Override
			public void setUnderlined(boolean underlined) {
				mUnderlined = underlined;
				mTextPaint.setUnderlineText(mUnderlined);
				createAndSetBitmap();
			}

			@Override
			public void setItalic(boolean italic) {
				mItalic = italic;
				updateTypeface();
				createAndSetBitmap();
			}

			@Override
			public void setBold(boolean bold) {
				mBold = bold;
				mTextPaint.setFakeBoldText(mBold);
				createAndSetBitmap();
			}

			@Override
			public void setTextSize(int size) {
				mTextSize = size;
				mTextPaint.setTextSize(mTextSize*mTextSizeMagnificationFactor);
				createAndSetBitmap();
			}
		};
		TextToolDialog.getInstance().setOnTextToolDialogChangedListener(mOnTextToolDialogChangedListener);
	}

	public void updateTypeface() {
		int style;
		if (mItalic) {
			style = Typeface.ITALIC;
		} else {
			style = Typeface.NORMAL;
		}

		if (mFont.equals("Sans Serif")) {
			mTextPaint.setTypeface(Typeface.create(Typeface.SANS_SERIF, style));
		} else if (mFont.equals("Serif")) {
			mTextPaint.setTypeface(Typeface.create(Typeface.SERIF, style));
		} else if (mFont.equals("Monospace")){
			mTextPaint.setTypeface(Typeface.create(Typeface.MONOSPACE, style));
		}

		if (Build.VERSION.SDK_INT < 21) {
			mTextPaint.setTextSkewX(0.0f);
			if (mFont.equals("Monospace")) {
				mTextPaint.setTypeface(Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL));
				if (style == Typeface.ITALIC) {
					mTextPaint.setTextSkewX(-0.25f);
				}
			}
		}
	}

	protected void changeTextColor() {
		float width = mBoxWidth;
		float height = mBoxHeight;
		PointF position = new PointF(mToolPosition.x, mToolPosition.y);
		mTextPaint.setColor(mCanvasPaint.getColor());
		createAndSetBitmap();
		mToolPosition = position;
		mBoxWidth = width;
		mBoxHeight = height;
	}

	@Override
	protected void resetInternalState() {
	}

	@Override
	protected void onClickInBox() {
		PointF toolPosition = new PointF(mToolPosition.x, mToolPosition.y);
		Command command = new TextToolCommand(mMultilineText, mTextPaint, mBoxOffset, mBoxWidth, mBoxHeight,
				toolPosition, mBoxRotation);
		((TextToolCommand) command).addObserver(this);
		IndeterminateProgressDialog.getInstance().show();
		PaintroidApplication.commandManager.commitCommand(command);
	}

	public void resetBoxPosition() {
		DrawingSurface surface = PaintroidApplication.drawingSurface;
		mToolPosition.x = surface.getBitmapWidth()/2.0f;
		mToolPosition.y = mBoxHeight/2.0f + mMarginTop;
	}

	@Override
	protected void drawToolSpecifics(Canvas canvas) {
	}

	@Override
	public int getAttributeButtonResource(TopBar.ToolButtonIDs buttonNumber) {
		switch (buttonNumber) {
			case BUTTON_ID_PARAMETER_TOP:
				return getStrokeColorResource();
			case BUTTON_ID_PARAMETER_BOTTOM_1:
				return R.drawable.icon_menu_text;
			case BUTTON_ID_PARAMETER_BOTTOM_2:
				return R.drawable.icon_menu_color_palette;
			default:
				return super.getAttributeButtonResource(buttonNumber);
		}
	}

	@Override
	public void attributeButtonClick(TopBar.ToolButtonIDs buttonNumber) {
		switch (buttonNumber) {
			case BUTTON_ID_PARAMETER_BOTTOM_1:
				showTextToolDialog();
				break;
			case BUTTON_ID_PARAMETER_TOP:
			case BUTTON_ID_PARAMETER_BOTTOM_2:
				showColorPicker();
				break;
			default:
				break;
		}
	}

}
