package com.example.project400.body_tracking

import android.graphics.PointF
import com.example.project400.body_tracking.BodyPart

data class KeyPoint(val bodyPart: BodyPart, var coordinate: PointF, val score: Float)
