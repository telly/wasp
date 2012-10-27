package com.telly.wasp.test;

import android.graphics.Bitmap;
import com.telly.wasp.BitmapHelper;
import junit.framework.TestCase;

import java.util.Random;

/**
 * Test to create several bitmaps
 *
 * @author Evelio Tarazona <evelio@evelio.info>
 * @version 1.0 10/26/12 1:35 PM
 */
public class CreateBitmapsTest extends TestCase {
    private static final int BIG_ASS_PX_WIDTH = 1300;
    private static final int BIG_ASS_PX_HEIGHT = 1300;
    private static final Bitmap.Config BIG_ASS_CONFIG = Bitmap.Config.ARGB_8888;
    private static final int BIG_ASS_COUNT = 5000;

    public void testBigAssBitmaps() throws Exception {
        for (int i = 0; i < BIG_ASS_COUNT; i++) {
            BitmapHelper.getInstance().createBitmap(BIG_ASS_PX_WIDTH, BIG_ASS_PX_HEIGHT, BIG_ASS_CONFIG);
        }
    }

    public void testRandomAssBitmaps() throws Exception {
        Random random = new Random(System.currentTimeMillis());
        for (int i = 0; i < BIG_ASS_COUNT; i++) {
            int randomAssHeight = random.nextInt(BIG_ASS_PX_HEIGHT);
            if (randomAssHeight < 1) {
                randomAssHeight = BIG_ASS_PX_HEIGHT;
            }
            int randomAssWidth = random.nextInt(BIG_ASS_PX_WIDTH);
            if (randomAssWidth < 1) {
                randomAssHeight = BIG_ASS_PX_WIDTH;
            }

            try {
                BitmapHelper.getInstance().createBitmap(randomAssHeight, randomAssHeight, BIG_ASS_CONFIG);
            } catch (IllegalStateException e) {
                System.out.println("Failed at bitmap #" + i);
                throw e;
            }
        }
    }
}
