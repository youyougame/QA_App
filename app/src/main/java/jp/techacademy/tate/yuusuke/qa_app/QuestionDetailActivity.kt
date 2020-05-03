package jp.techacademy.tate.yuusuke.qa_app

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v7.app.AppCompatActivity
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ListView

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_question_detail.*
import kotlinx.android.synthetic.main.activity_question_send.*

import java.util.HashMap

class QuestionDetailActivity : AppCompatActivity() {

    private lateinit var mQuestion: Question
    private lateinit var mFavorite: Favorite
    private lateinit var mAdapter: QuestionDetailListAdapter
    private lateinit var mAnswerRef: DatabaseReference
    private var checkFavorite = "notFavorite"
    private lateinit var mDatabaseReference: DatabaseReference
    private var mGenre = 0


    private val mEventListener = object : ChildEventListener {
        override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {

            val map = dataSnapshot.value as Map<String, String>

            val answerUid = dataSnapshot.key ?: ""

            for (answer in mQuestion.answers) {
                // 同じAnswerUidのものが存在しているときは何もしない
                if (answerUid == answer.answerUid) {
                    return
                }
            }

            val body = map["body"] ?: ""
            val name = map["name"] ?: ""
            val uid = map["uid"] ?: ""

            val answer = Answer(body, name, uid, answerUid)
            mQuestion.answers.add(answer)
            mAdapter.notifyDataSetChanged()
        }

        override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {

        }

        override fun onChildRemoved(dataSnapshot: DataSnapshot) {

        }

        override fun onChildMoved(dataSnapshot: DataSnapshot, s: String?) {

        }

        override fun onCancelled(databaseError: DatabaseError) {

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_question_detail)

        val numExtra = intent.extras!!
        val number = numExtra.get("num")

        if (number == 2) {
            Log.d("kotlintest", "1通過")
            val userId = FirebaseAuth.getInstance().currentUser!!.uid

            val favExtras = intent.extras
            Log.d("kotlintest", "2通過")
            mFavorite = favExtras.get("favorite") as Favorite
            val dataBaseReference = FirebaseDatabase.getInstance().reference
            val favoriteRef = dataBaseReference.child(FavoritePATH).child(userId)
            favoriteRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    Log.d("kotlintest", "3通過")
                    val map = dataSnapshot.value as Map<String, String>
                    mGenre = map["genre"]!!.toInt()
                }

                override fun onCancelled(p0: DatabaseError) {

                }
            })


            val questionRef = dataBaseReference.child(ContentsPATH).child(mGenre.toString()).child(mFavorite.questionUid)
            Log.d("kotlintest", "4通過")
            questionRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    Log.d("kotlintest", "5通過")
                    val map = dataSnapshot.value as Map<String, String>
                    val title = map["title"] ?: ""
                    val body = map["body"] ?: ""
                    val name = map["name"] ?: ""
                    val uid = map["uid"] ?: ""
                    val imageString = map["image"] ?: ""
                    val bytes =
                        if (imageString.isNotEmpty()) {
                            Base64.decode(imageString, Base64.DEFAULT)
                        } else {
                            byteArrayOf()
                        }

                    val answerArrayList = ArrayList<Answer>()
                    val answerMap = map["answers"] as Map<String, String>?
                    if (answerMap != null) {
                        for (key in answerMap.keys) {
                            val temp = answerMap[key] as Map<String, String>
                            val answerBody = temp["body"] ?: ""
                            val answerName = temp["name"] ?: ""
                            val answerUid = temp["uid"] ?: ""
                            val answer = Answer(answerBody, answerName, answerUid, key)
                            answerArrayList.add(answer)
                        }
                    }

                    val question = Question(
                        title, body, name, uid, dataSnapshot.key ?: "",
                        mGenre, bytes, answerArrayList
                    )
                    Log.d("kotlintest", "Q" + question.toString())
                    mQuestion = question
                    mAdapter.notifyDataSetChanged()

                }

                override fun onCancelled(p0: DatabaseError) {

                }
            })
        } else {


            // 渡ってきたQuestionのオブジェクトを保持する
            val extras = intent.extras
            mQuestion = extras.get("question") as Question

        }

        title = mQuestion.title

        // ListViewの準備
        mAdapter = QuestionDetailListAdapter(this, mQuestion)
        listView.adapter = mAdapter
        mAdapter.notifyDataSetChanged()

        fab.setOnClickListener {
            // ログイン済みのユーザーを取得する
            val user = FirebaseAuth.getInstance().currentUser

            if (user == null) {
                // ログインしていなければログイン画面に遷移させる
                val intent = Intent(applicationContext, LoginActivity::class.java)
                startActivity(intent)
            } else {
                // Questionを渡して回答作成画面を起動する
                val intent = Intent(applicationContext, AnswerSendActivity::class.java)
                intent.putExtra("question", mQuestion)
                startActivity(intent)
            }
        }

        val dataBaseReference = FirebaseDatabase.getInstance().reference
        mAnswerRef = dataBaseReference.child(ContentsPATH).child(mQuestion.genre.toString()).child(mQuestion.questionUid).child(AnswersPATH)
        mAnswerRef.addChildEventListener(mEventListener)

        val favoriteButton = findViewById<FloatingActionButton>(R.id.favoriteFab)

        val favoriteFab = findViewById<FloatingActionButton>(R.id.favoriteFab)
        favoriteFab.setOnClickListener { view ->
            mDatabaseReference = FirebaseDatabase.getInstance().reference
            val userRef = mDatabaseReference.child(FavoritePATH).child(mQuestion.uid).child(mQuestion.questionUid)//.child(mQuestion.genre.toString())

            if (checkFavorite == "notFavorite") {
                checkFavorite = "Favorite"
                favoriteButton.setImageResource(R.drawable.favorite_star_yellow)
                val data = HashMap<String, String>()
                data["title"] = mQuestion.title
                data["body"] = mQuestion.body
                data["name"] = mQuestion.name
                data["uid"] = mQuestion.uid
                data["genre"] = mQuestion.genre.toString()
                data["questionUid"] = mQuestion.questionUid


                userRef.setValue(data)

            } else {
                checkFavorite = "notFavorite"
                favoriteButton.setImageResource(R.drawable.favorite_star_gray)
                userRef.setValue(null)

            }
        }


    }

    @SuppressLint("RestrictedApi")
    override fun onResume() {
        super.onResume()
        val favoriteButton = findViewById<FloatingActionButton>(R.id.favoriteFab)

        val user = FirebaseAuth.getInstance().currentUser

        if (user == null) {
            favoriteButton.visibility = View.INVISIBLE
        } else {
            favoriteButton.visibility = View.VISIBLE
        }

        mDatabaseReference = FirebaseDatabase.getInstance().reference
        val userRef = mDatabaseReference.child(FavoritePATH).child(mQuestion.uid).child(mQuestion.questionUid)//.child(mQuestion.genre.toString())

        userRef.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val value = dataSnapshot.value

                if (value != null) {
                    checkFavorite = "Favorite"
                    favoriteButton.setImageResource(R.drawable.favorite_star_yellow)
                }
            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })

    }


}
