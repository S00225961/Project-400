package com.example.project400.data

import android.graphics.PointF
import com.example.project400.data.BodyPart

data class KeyPoint(val bodyPart: BodyPart, var coordinate: PointF, val score: Float)
