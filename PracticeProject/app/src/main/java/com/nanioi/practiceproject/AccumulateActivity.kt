package com.nanioi.practiceproject

import android.app.DatePickerDialog
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.widget.DatePicker
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_accumulate.*
import java.util.*

class AccumulateActivity : AppCompatActivity() {
    var dt : Int = 0
    override fun onCreate(savedInstanceState: Bundle?) {// 화면 보이는 부분
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_accumulate)

        summary_btn.setOnClickListener { // 버튼 클릭 시 액티비티 이동
            startActivity(Intent(this, SummaryActivity::class.java))
        }
        new_btn.setOnClickListener {
            startActivity(Intent(this, NewActivity::class.java))
        }
        precaution_btn.setOnClickListener {
            startActivity(Intent(this, PrecautionActivity::class.java))
        }
        calendar_btn.setOnClickListener { // 캘린더 날짜 설정하는 버튼 클릭 시
            val today = GregorianCalendar()
            val year: Int = today.get(Calendar.YEAR)
            val month: Int = today.get(Calendar.MONTH)
            val date: Int = today.get(Calendar.DATE)
            val dlg = DatePickerDialog(this, object : DatePickerDialog.OnDateSetListener {
                override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
                    dt =0
                    dt+=year
                    dt*=100
                    dt+=(month+1)
                    dt*=100
                    dt+=dayOfMonth
                    XmlParsingTask().execute()
                }
            }, year, month, date)
            dlg.show() // 캘린 더 보여주는 부분
        }
        XmlParsingTask().execute()
    }
    inner class XmlParsingTask() : AsyncTask<Any?, Any?, List<ResponseElement>>() {
        override fun onPostExecute(result: List<ResponseElement>) {
            super.onPostExecute(result)

            // xml로 받아온 결과 저장하는 부분 -> 이건 예시 xml 코드 보고 이해해야할
            val data: ResponseElement = result[0]
            val data_prev: ResponseElement = result[1]
            val decide_new_cnt = ((data.decideCnt!!).toInt() - (data_prev.decideCnt!!).toInt())
            val exam_new_cnt = ((data.examCnt!!).toInt() - (data_prev.examCnt!!).toInt())
            val clear_new_cnt = ((data.clearCnt!!).toInt() - (data_prev.clearCnt!!).toInt())
            val death_new_cnt = ((data.deathCnt!!).toInt() - (data_prev.deathCnt!!).toInt())

            decide.setText(data.decideCnt) // decide 나 exam 이나 이런 것들 다 res->layout-> accumulatelayout 거부분에 id로 표시된 부분들이여
            decide_new.setText("+" + decide_new_cnt.toString()) // 그 화면에 ( xml res -> layout -> accumulate 부분 들어가면 dicide_new라는 id 부분의 공간에 텍스트 넣어주는 거, 다른 코드들도 그렇고  )
            exam.setText(data.examCnt)
            if(exam_new_cnt >= 0){
                exam_new.setText("+" + exam_new_cnt.toString())
            }else{
                exam_new.setText(exam_new_cnt.toString())
            }
            clear.setText(data.clearCnt)
            clear_new.setText("+" + clear_new_cnt.toString())
            death.setText(data.deathCnt)
            death_new.setText("+" + death_new_cnt.toString())
            state_dt.setText(data.stateDt)
            state_time.setText(data.stateTime)

            setLineChart(accumulate_chart,result,this@AccumulateActivity)
        }
        override fun doInBackground(vararg params: Any?): List<ResponseElement> { // xml 파싱할때 여기서 데이터 받아와 reedFeed 부분은 저 XmlParsingTask 파일보면 있으
            return readFeed(parsingData(dt))
        }
    }
}