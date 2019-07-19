package jp.ac.asojuku.st.noffication_de_study

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import kotlinx.android.synthetic.main.fragment_fragment_my_record.*

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

private lateinit var contextIn: Context

class FragmentMyRecord : Fragment() {
    private var param1: String? = null
    private var param2: String? = null
    private var listener: OnFragmentInteractionListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    inner class StatisticsItem {
        var id: Long = 0
        var question: String? = null
        var correctCount: String? = null
        var answerCount: String? = null
    }

    inner class StatisticsAdapter(context: Context) : BaseAdapter() {
        private var layoutInflater: LayoutInflater? = null
        private lateinit var statisticsItem: ArrayList<StatisticsItem>

        init {
            this.layoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        }

        fun setStatisticsItem(statisticsItem: ArrayList<StatisticsItem>) {
            this.statisticsItem = statisticsItem
        }

        override fun getCount(): Int {
            return try {
                statisticsItem.size
            } catch (e: Exception) {
                0
            }
        }

        override fun getItem(position: Int): Any {
            return statisticsItem[position]
        }

        override fun getItemId(position: Int): Long {
            return statisticsItem[position].id
        }

        @SuppressLint("ViewHolder")
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = layoutInflater!!.inflate(R.layout.layout_statistics_activity_list, parent, false)

            val outSolve = if (statisticsItem[position].answerCount!!.toInt() == 0) {
                "未回答"
            } else {
                "回答済み"
            }

            (view.findViewById<View>(R.id.id) as TextView).text = statisticsItem[position].id.toString()
            (view.findViewById<View>(R.id.flg) as TextView).text = outSolve
            (view.findViewById<View>(R.id.count) as TextView).text =
                String.format("%s / %s", statisticsItem[position].correctCount, statisticsItem[position].answerCount)

            return view
        }
    }


    override fun onResume() {
        super.onResume()

        val helper = SQLiteHelper(contextIn)
        val db = helper.readableDatabase
        val query = "SELECT * FROM exams_numbers"
        val cursor = db.rawQuery(query, null)
        cursor.moveToFirst()

        val examNameArray: ArrayList<String> = arrayListOf()
        for (i in 0 until cursor.count) {
            val examName = cursor.getString(cursor.getColumnIndex("exams_number"))
            examNameArray.add(examName)
            cursor.moveToNext()
            if (i == 0) {
                setListView(examName)
            }
        }
        val adapter = ArrayAdapter(contextIn, android.R.layout.simple_spinner_item, examNameArray)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        exam_id_select.adapter = adapter
        cursor.close()

        exam_id_select.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val spinner = parent as Spinner
                val select = spinner.selectedItem.toString()
                setListView(select)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
        }
    }

    fun setListView(examName: String) {
        val helper = SQLiteHelper(contextIn)
        val db = helper.readableDatabase
        val listViewQuery = "SELECT correct.question_id as question_id , correct.question as question, SUM(CASE WHEN correct.answer_choice IS NULL OR correct.answer_choice IS 9999 THEN 0 ELSE (CASE WHEN correct.answer_choice IS correct.answer_number THEN 1 ELSE 0 END )END) as correct_count, COUNT(answer_choice) as answer_count" +
                " FROM ((((select question_id from exams_questions where exams_number = ?)AS qId" +
                " LEFT JOIN user_answers as ua ON qId.question_id = ua.question_id) as count" +
                " LEFT JOIN questions as q ON count.question_id = q.question_id) as text" +
                " LEFT JOIN answers as a ON text.question_id = a.question_id) as correct" +
                " GROUP BY correct.question_id " +
                "ORDER BY correct.question_id"
        val listViewQueryCursor = db.rawQuery(listViewQuery, arrayOf(examName))
        listViewQueryCursor.moveToFirst()
        val list = ArrayList<StatisticsItem>()
        for (i in 0 until listViewQueryCursor.count) {
            val questionId = listViewQueryCursor.getString(listViewQueryCursor.getColumnIndex("question_id"))
            val question = listViewQueryCursor.getString(listViewQueryCursor.getColumnIndex("question"))
            val correctCount = listViewQueryCursor.getString(listViewQueryCursor.getColumnIndex("correct_count"))
            val answerCount = listViewQueryCursor.getString(listViewQueryCursor.getColumnIndex("answer_count"))

            val statisticsItem = StatisticsItem()
            statisticsItem.id = questionId.toLong()
            statisticsItem.question = question
            statisticsItem.correctCount = correctCount
            statisticsItem.answerCount = answerCount
            list.add(statisticsItem)

            listViewQueryCursor.moveToNext()
        }
        listViewQueryCursor.close()

        val listView = list_view
        val statisticsAdapter = StatisticsAdapter(contextIn)
        statisticsAdapter.setStatisticsItem(list)
        statisticsAdapter.notifyDataSetChanged()
        listView.adapter = statisticsAdapter

        listView.setOnItemClickListener { parent, _, position, _ ->
            val item = parent.getItemAtPosition(position) as StatisticsItem
            val questionId = item.id

            val examData = ExamData(2, "FE", "FE10901")
            examData.question_list.add(questionId.toInt())

            val intent = Intent(activity, QuestionActivity::class.java)
            intent.putExtra("exam_data", examData)
            startActivity(intent)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_fragment_my_record, container, false)
    }

    fun onButtonPressed(uri: Uri) {
        listener?.onFragmentInteraction(uri)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        contextIn = context
        if (context is OnFragmentInteractionListener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement OnFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    interface OnFragmentInteractionListener {
        fun onFragmentInteraction(uri: Uri)
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            FragmentMyRecord().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
