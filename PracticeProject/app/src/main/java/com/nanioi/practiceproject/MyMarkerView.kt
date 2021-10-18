package com.nanioi.practiceproject

import android.content.Context
import android.graphics.Canvas
import android.widget.LinearLayout
import android.widget.TextView
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight

class MyMarkerView : MarkerView { // 이건 차트 클릭할때 그 데이터 숫자 부분 마크 표시 해주는 부분
    private lateinit var Content1: TextView

    constructor(context: Context?, layoutResource: Int) : super(context, layoutResource) {
        Content1 = findViewById(R.id.marker_text)
    }
    override fun refreshContent(e: Entry?, highlight: Highlight?) { //좌표 받아와서 text띄우는 거
        Content1.text = (e?.y!!.toInt()).toString()
        super.refreshContent(e, highlight)
    }

    //marker 위치 상단 중앙으로 조정
    override fun draw(canvas: Canvas?) {
        canvas!!.translate(-(width / 2).toFloat(), -height.toFloat() )
        super.draw(canvas)
    }

}
