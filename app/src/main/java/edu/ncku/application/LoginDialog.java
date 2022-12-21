package edu.ncku.application;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Spannable;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.method.PasswordTransformationMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

import edu.ncku.application.fragments.BottomSheetFragment;
import edu.ncku.application.io.IOConstatnt;
import edu.ncku.application.io.network.LoginTask;
import edu.ncku.application.util.DrawerListSelector;
import edu.ncku.application.util.EnvChecker;
import edu.ncku.application.util.PreferenceKeys;

public class LoginDialog extends DialogFragment implements IOConstatnt{

    private static final String DEBUG_FLAG  = LoginDialog.class.getName();

    /* UI components */
    private Button mBtnLogin;
    private EditText mEditUsername, mEditPassword;
    //20200422 Add check box
    private CheckBox mCheckBoxPrivacy;
    //20200429 Add privacy textView
    private TextView mTextPrivacy;
    private TextView mForgotPwd;
    public TextView mTxtTip;
    public ProgressBar mPBLogin;

    private DrawerListSelector drawerListSelector;
    private Context context;
    private Handler handler = new Handler();
    //20200429 Add parameter
    private MainActivity activity;

    // 20221007: login constraint, 3 attempts in 3 minutes, block user 15 minutes
    private static final int ATTEMPT_TIME_LIMIT = 3 * 60 * 1000;
    private static final int BLOCK_TIME = 15 * 60 * 1000;
    public boolean loginState = false;
    private SharedPreferences sharedPreferences;

    public LoginDialog(){}
    /**
     * Constructor
     *
     * @param drawerListSelector
     * @param context
     */
    @SuppressLint("ValidFragment")
    //20200429 Add third parameter "MainActivity"
    public LoginDialog(DrawerListSelector drawerListSelector, Context context, MainActivity activity) {
        this.drawerListSelector = drawerListSelector;
        this.context = context;
        this.activity = activity;
        this.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflater.inflate(R.layout.fragment_login, null);
        mBtnLogin = (Button) v.findViewById(R.id.btnLogin);
        mEditUsername = (EditText) v.findViewById(R.id.editTextID);
//        mEditUsername.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);

        mEditPassword = (EditText) v.findViewById(R.id.editTextPassword);
        mEditPassword.setTypeface(Typeface.DEFAULT);
        mEditPassword.setTransformationMethod(new PasswordTransformationMethod());
//        mEditPassword.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        // filter used to limit user enter the digits and alphabets
        mEditPassword.setFilters(new InputFilter[] {
            new InputFilter() {
                @Override
                public CharSequence filter(CharSequence src, int start,
                                           int end, Spanned dst, int dstart, int dend) {
                    if(src.equals("")){ // for empty string
                        return src;
                    }
                    for (int i = start; i < end; i++) {
                        // check space or star symbol
                        if (Character.isSpaceChar(src.charAt(i)) | src.charAt(i)=='*') {
                            return "";
                        }
                    }
                    return src;
                }
            }
        });
        mTxtTip = (TextView) v.findViewById(R.id.txtTip);
        mPBLogin = (ProgressBar) v.findViewById(R.id.progressBarLogin);
        //20200422 Link check box
        mCheckBoxPrivacy = (CheckBox) v.findViewById(R.id.checkBoxPrivacy);
        //20200429 Link textView
        mTextPrivacy= (TextView) v.findViewById(R.id.tv_checkprivacy);
        mForgotPwd = (TextView) v.findViewById(R.id.forgot_pwd);
        //20200429 Init UI
        init_login_UI();

        mCheckBoxPrivacy.setOnClickListener(view -> {
            if (mCheckBoxPrivacy.isChecked())
            {
                mBtnLogin.getBackground().setColorFilter(null);
                mBtnLogin.setEnabled (true);
            }
            else
            {
                mBtnLogin.getBackground().setColorFilter(Color.LTGRAY, PorterDuff.Mode.SRC_IN);
                mBtnLogin.setEnabled(false);
            }
        });

        setLonginButtonListenner();

		/* 建立AlertDialog實體 */
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        return builder.setView(v).create();
    }

    private void setLonginButtonListenner() {
        // TODO Auto-generated method stub
        mBtnLogin.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                setLogining(true); // 將狀態變成登入中
                // 在三分鐘內嘗試三次失敗，停用登入功能15分鐘
                Long last_attempt_time = sharedPreferences.getLong(PreferenceKeys.ATTEMPT_LOGIN_TIME,0);
                Integer attempt_num = sharedPreferences.getInt(PreferenceKeys.ATTEMPT_LOGIN_NUM,0);
                if(new Date().getTime() - last_attempt_time < BLOCK_TIME && attempt_num >= 3){
                    Toast.makeText(context, R.string.login_attempt_3_times, Toast.LENGTH_SHORT).show();
                    setLogining(false);
                }
                else {
                    /* 從設定值取得帳號跟密碼 */
                    final String username = mEditUsername.getText().toString();
                    final String password = mEditPassword
                            .getText().toString();

                    /* 判斷是否有欄位沒有填寫 */
                    if (("".equals(username)) || ("".equals(password))) {
                        handler.post(() -> {
                            mTxtTip.setText(R.string.invalid_account_or_password);
                            mEditPassword.setText("");
                        });
                        setLogining(false);
                        return;
                    }
                    /*20200304 移除密碼輸入時，對於特殊自元的限制
                    String[] invalidCharacters = {"*", "\'", ";", "\"", "=", "@", "$", "|", "&"};
                    if(stringContainsItemFromList(password, invalidCharacters)){
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                mTxtTip.setText(R.string.password_contains_invalid_character);
                                mEditPassword.setText("");
                            }
                        });
                        setLogining(false);
                        return;
                    }
                    */

                    /* 判斷網路是否有連線 */
                    if (!EnvChecker.isNetworkConnected(context)) {
                        mTxtTip.setText(R.string.network_disconnected);
                        setLogining(false);
                        return;
                    }

                    LoginTask loginTask = new LoginTask(context, LoginDialog.this, drawerListSelector);
                    String login_result = "";
                    try {
                        login_result = loginTask.executeOnExecutor(
                                AsyncTask.THREAD_POOL_EXECUTOR, username, password).get();
                    } catch (ExecutionException | InterruptedException e) {
                        e.printStackTrace();
                    }

                    Log.d("LoginState", login_result);
                }
            }

            private boolean stringContainsItemFromList(String password, String[] invalidCharacters) {
                for(String character:invalidCharacters){
                    if(password.contains(character)){
                        return true;
                    }
                }
                return false;
            }

        });
    }

    /**
     * @param logining 設定當前是否處在登入中的狀態
     */
    public synchronized void setLogining(final boolean logining) {

        handler.postDelayed(() -> {
            /* 登入按鈕的轉換 */
            mPBLogin.setVisibility((logining) ? View.VISIBLE : View.INVISIBLE);
            mBtnLogin.setVisibility((logining) ? View.INVISIBLE : View.VISIBLE);
        }, (logining) ? 0 : 2000);

        loginState = logining;
    }

    @Override
    public void dismiss() {
        super.dismiss();
        // 重新繪圖，讓設定按鈕顯示出來
        getActivity().invalidateOptionsMenu();
    }

    //20200429 Added
    public void init_login_UI(){
        //To check current language and then use different "Spannable" setting
        String cur_language = Locale.getDefault().getLanguage();
        Spannable span_policy = Spannable.Factory.getInstance().newSpannable(getResources().getString(R.string.login_privacy));
        Spannable span_forgot_pwd = Spannable.Factory.getInstance().newSpannable(getResources().getString(R.string.forgot_pwd));
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View textView) {
                //20200429 顯示bottom sheet fragment
                BottomSheetFragment addBottomDialogFragment = BottomSheetFragment.newInstance();
                addBottomDialogFragment.show(activity.getSupportFragmentManager(), "add_bottom_dialog_fragment");
                if(showLogMsg){
                    Log.d("main", "Privacy Policy");
                }
                //Toast.makeText(context, "Privacy Policy", Toast.LENGTH_SHORT).show();
                mCheckBoxPrivacy.setEnabled(true);
            }
        };
        ClickableSpan forgot_pwd_span = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View view) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.addCategory(Intent.CATEGORY_BROWSABLE);
                if (cur_language.equals("en")){
                    intent.setData(Uri.parse("https://i.ncku.edu.tw/en/user/password"));
                }else{
                    intent.setData(Uri.parse("https://i.ncku.edu.tw/zh-hant/user/password"));
                }
                startActivity(intent);
            }
        };

        if (cur_language.equals("en")) {
            span_policy.setSpan(clickableSpan, 28, getResources().getString(R.string.login_privacy).trim().length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        else {
            span_policy.setSpan(clickableSpan, 9, 14, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        span_forgot_pwd.setSpan(forgot_pwd_span, 0, getResources().getString(R.string.forgot_pwd).trim().length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        mTextPrivacy.setText(span_policy);
        mTextPrivacy.setMovementMethod(LinkMovementMethod.getInstance());
        mForgotPwd.setText(span_forgot_pwd);
        mForgotPwd.setMovementMethod(LinkMovementMethod.getInstance());
        mCheckBoxPrivacy.setEnabled(false);
        mBtnLogin.setEnabled (false);
        mBtnLogin.getBackground().setColorFilter(Color.LTGRAY, PorterDuff.Mode.SRC_IN);
    }
}
