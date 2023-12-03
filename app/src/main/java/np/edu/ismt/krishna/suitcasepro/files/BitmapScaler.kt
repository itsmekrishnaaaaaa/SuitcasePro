package np.edu.ismt.krishna.suitcasepro.files

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.RectF

class BitmapScalar {
    companion object {
        // scale and keep aspect ratio
        fun scaleToFitWidth(b: Bitmap, width: Int): Bitmap? {
            val factor = width / b.width.toFloat()
            return Bitmap.createScaledBitmap(b, width, (b.height * factor).toInt(), true)
        }


        // scale and keep aspect ratio
        fun scaleToFitHeight(b: Bitmap, height: Int): Bitmap? {
            val factor = height / b.height.toFloat()
            return Bitmap.createScaledBitmap(b, (b.width * factor).toInt(), height, true)
        }


        // scale and keep aspect ratio
        fun scaleToFill(b: Bitmap, width: Int, height: Int): Bitmap? {
            val factorH = height / b.width.toFloat()
            val factorW = width / b.width.toFloat()
            val factorToUse = if (factorH > factorW) factorW else factorH
            return Bitmap.createScaledBitmap(
                b,
                (b.width * factorToUse).toInt(),
                (b.height * factorToUse).toInt(),
                true
            )
        }

        fun getCircularBitmap(bitmap: Bitmap): Bitmap {
            val output = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(output)

            val color = Color.RED
            val paint = Paint()
            val rect = Rect(0, 0, bitmap.width, bitmap.height)
            val rectF = RectF(rect)

            paint.isAntiAlias = true
            canvas.drawARGB(0, 0, 0, 0)
            paint.color = color
            canvas.drawOval(rectF, paint)

            paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
            canvas.drawBitmap(bitmap, rect, rect, paint)


            return output
        }
        // scale and don't keep aspect ratio
        fun stretchToFill(b: Bitmap, width: Int, height: Int): Bitmap? {
            val factorH = height / b.height.toFloat()
            val factorW = width / b.width.toFloat()
            return Bitmap.createScaledBitmap(
                b,
                (b.width * factorW).toInt(),
                (b.height * factorH).toInt(),
                true
            )
        }
    }
}