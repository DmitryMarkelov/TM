package com.tagmarshal.golf.fragment.leaderboard;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.tagmarshal.golf.R;
import com.tagmarshal.golf.fragment.BaseFragment;
import com.tagmarshal.golf.manager.PreferenceManager;
import com.tagmarshal.golf.rest.model.RestInRoundModel;
import com.tagmarshal.golf.util.TMUtil;
import com.tagmarshal.golf.view.TMButton;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class LeaderBoardFragment extends BaseFragment  implements LeaderBoardContract.View {

    @BindView(R.id.top_margin)
    View mTopMargin;

    @BindView(R.id.webview_webview)
    WebView leaderboardWebView;


    @BindView(R.id.webview_backBtn)
    TMButton scoreBackBtn;

    Unbinder unbinder;

    private LeaderBoardContract.Presenter presenter;

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if(hidden){
            getBaseActivity().showAppbar(true);
        }else{
            getBaseActivity().showAppbar(false);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_web_view, container, false);

        unbinder = ButterKnife.bind(this, view);

        presenter = new LeaderBoardPresenter(this);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mTopMargin.getLayoutParams().height = TMUtil.getTopMargin();
       leaderboardWebView.getSettings().setJavaScriptEnabled(true);
       leaderboardWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return false;
            }

       });
       leaderboardWebView.setWebChromeClient(new WebChromeClient() {

       });

       onGetRoundInfo(PreferenceManager.getInstance().getPaceInfo());
    }

    @Override
    public void onGetRoundInfo(RestInRoundModel restInRoundModel) {
        leaderboardWebView.loadUrl("https://rest.tagmarshal.golf/courses/leaderboards/"+restInRoundModel.getGolfGeniusId()+"?baseUrl="+PreferenceManager.getInstance().getCourse());
    }

    @OnClick(R.id.webview_backBtn)
    public void onBackClick(){
        getBaseActivity().onBackPressed();
    }

    @Override
    public void onFailure(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showLoading(boolean show) {
        getBaseActivity().showPleaseWaitDialog(show);
    }

    @Override
    public void onDestroyView() {
        presenter.onDestroy();


        unbinder.unbind();
        unbinder = null;

        super.onDestroyView();
    }
}
