package edu.ncku.application.fragments;

import android.os.Bundle;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import edu.ncku.application.R;

public class BottomSheetFragment extends BottomSheetDialogFragment{
    private TextView tv_privacy;

    public static BottomSheetFragment newInstance() {
        return new BottomSheetFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bottom_sheet, container, false);
        tv_privacy = (TextView) view.findViewById(R.id.tv_policy);
        tv_privacy.setMovementMethod(new ScrollingMovementMethod());
        //20200720 讓textview超連結可以作用
        tv_privacy.setMovementMethod(LinkMovementMethod.getInstance());

        //20200521 新增隱私權政策到strings.xml
        //String privacy_content = "<body dir=\"ltr\" style=\"max-width:21.001cm;margin-top:2cm; margin-bottom:2cm; margin-left:2cm; margin-right:2cm; \"><p class=\"P2\">隱私權政策</p><p class=\"Standard\">親愛的教職員與同學們，感謝您使用成功大學圖書館APP，您個人的隱私權，成功大學圖書館APP絕對尊重並予以保護。為了幫助您瞭解成功大學圖書館APP如何保護您上網的權益，請您詳細閱讀下列資訊：</p><p class=\"Standard\"> </p><p class=\"P1\">關於政策適用範圍</p><p class=\"Standard\">以下的隱私權政策，適用於您在成功大學圖書館APP活動時，所涉及的個人資料蒐集、運用與保護。凡經由成功大學圖書館APP連結之網站，各網站均有其專屬之隱私權政策，成功大學圖書館APP不負任何連帶責任。當您連結這些網站時，關於個人資料的保護，適用各網站的隱私權政策。</p><p class=\"Standard\"> </p><p class=\"P1\">關於個人資料之蒐集</p><p class=\"Standard\"><span> 1</span><span> 單純在成功大學圖書館APP的瀏覽及相關行為，本應用程式並不會蒐集任何有關個人的身份資料。</span></p><p class=\"Standard\"><span> 2</span><span> 應用程式會記錄使用者上網時間，這些資料係供成功大學圖書館APP內部作流量和網路行為調查，以利於提昇本應用程式的服務品質，且成功大學圖書館APP僅對全體使用者行為總和進行分析，並不會對個別使用者進行分析。</span></p><p class=\"Standard\"><span> 3</span><span> 成功大學圖書館APP有義務保護各申請人隱私，非經您本人同意不會自行修改或刪除任何個人資料及檔案。</span></p><p class=\"Standard\"><span> 4</span><span> 為推廣成功大學圖書館APP相關服務與行銷目的，本館會使用裝置的識別碼做為ID，用以進行推播訊息發送使用。</span></p><p class=\"Standard\"> </p><p class=\"P1\">與第三者資料共用政策</p><p class=\"Standard\">成功大學圖書館APP絕不會任意提供出售、交換、或出租任何您的個人資料給其他團體、個人或私人企業。但有下列情形者除外：</p><p class=\"Standard\"><span> 1</span><span> 中華民國司法檢調單位透過合法程序進行調閱；</span></p><p class=\"Standard\"><span> 2</span><span> 違反應用程式或網站相關規章且已造成脅迫性；</span></p><p class=\"Standard\"><span> 3</span><span> 基於主動衍伸應用程式或網站服務效益之考量。 引用本館應用程式資料應注意事項 本館應用程式中所有資料（包括圖檔及文字檔），著作權皆屬於成功大學圖書館APP所有（成功大學圖書館APP連結至外部之網站除外），任何網站以超連結方式連結至本館應用程式，毋須經過本館同意；引用本館應用程式中之任何資料，請先函本館或以電子郵件方式寄至本館服務信箱，徵得本館同意後方得引用，引用時，並請註明出處。</span></p><p class=\"Standard\"> </p><p class=\"P1\">隱私權保護宣告之修訂暨諮詢</p><p class=\"Standard\">本應用程式會不定時修訂本項政策，以符合最新之隱私權保護規範。當在使用個人資料的規定做較大幅度修改時，會在網頁上張貼告示，通知您相關事項。如果您對成功大學圖書館APP的隱私權保護宣告有任何疑問，歡迎與我們聯絡。</p></body>";
        String privacy_content = getString(R.string.privacy_policy);
        tv_privacy.setText(Html.fromHtml(privacy_content));

        return view;
    }

}
