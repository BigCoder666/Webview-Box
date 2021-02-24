package com.tx.webbox;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class WebBoxView extends WebView {
    String url = "";
    IFinish iFinish=null;
    Activity activity =null;

    List<String> findKeyList = Arrays.asList(
            "a",
            "abbr",
            "acronym",
            "address",
            "applet",
            "area",
            "article",
            "aside",
            "audio",
            "b",
            "base",
            "basefont",
            "bdi",
            "bdo",
            "big",
            "blockquote",
            "body",
            "br",
            "button",
            "canvas",
            "caption",
            "center",
            "cite",
            "code",
            "col",
            "colgroup",
            "command",
            "datalist",
            "dd",
            "del",
            "details",
            "dir",
            "div",
            "dfn",
            "dialog",
            "dl",
            "dt",
            "em",
            "embed",
            "fieldset",
            "figcaption",
            "figure",
            "font",
            "footer",
            "form",
            "frame",
            "frameset",
            "h1",
            "h2",
            "h3",
            "h4",
            "h5",
            "h6",
            "head",
            "header",
            "hr",
            "html",
            "i",
            "iframe",
            "img",
            "input",
            "ins",
            "isindex",
            "kbd",
            "keygen",
            "label",
            "legend",
            "li",
            "link",
            "map",
            "mark",
            "menu",
            "meta",
            "meter",
            "nav",
            "noframes",
            "noscript",
            "object",
            "ol",
            "optgroup",
            "option",
            "output",
            "p",
            "param",
            "pre",
            "progress",
            "q",
            "rp",
            "rt",
            "ruby",
            "s",
            "samp",
            "script",
            "section",
            "select",
            "small",
            "source",
            "span",
            "strike",
            "strong",
            "style",
            "sub",
            "summary",
            "sup",
            "table",
            "tbody",
            "td",
            "textarea",
            "tfoot",
            "th",
            "thead",
            "time",
            "title",
            "tr",
            "track",
            "tt",
            "u",
            "ul",
            "var",
            "video",
            "wbr",
            "xmp");

    public WebBoxView(@NonNull Context context) {
        super(context);
    }

    public WebBoxView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public WebBoxView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public WebBoxView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void loadUrl(Activity activity,String url, IFinish iFinish) {
        this.url = url;
        this.iFinish = iFinish;
        this.activity = activity;
        stopLoading();
        setWebChromeClient(null);
        setWebViewClient(null);
        clearCache(true);//清除缓存
        clearHistory();
        // 此方法需要启用JavaScript
        getSettings().setJavaScriptEnabled(true);
        getSettings().setBlockNetworkImage(true);//解决图片不显示
        getSettings().setUserAgentString("Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/43.0.2357.134 Safari/537.36");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);//解决图片不显示
        }
        // 把刚才的接口类注册到名为HTMLOUT的JavaScript接口
        addJavascriptInterface(new ToModelJavaScriptInterface(), "HTMLOUT");
        // 必须在loadUrl之前设置WebViewClient
        WebViewClient webViewClient = new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                if (getProgress() == 100) {
                    loadUrl("javascript:HTMLOUT.print(document.documentElement.outerHTML);");
                }
            }
        };
        setWebViewClient(webViewClient);
        setWebChromeClient(new WebChromeClient());
        loadUrl(url);
    }

    public interface IFinish {
        void finish(String url,WebInfo webInfo);
    }

    public class ToModelJavaScriptInterface {

        public ToModelJavaScriptInterface() {

        }

        @JavascriptInterface
        @SuppressWarnings("unused")
        public void print(final String html) {
            WebInfo mainWebMap =new WebInfo();
            ArrayList<String> selectKeyList = new ArrayList<>();

            Document doc;
            doc = Jsoup.parse(html);

            Elements mainWeb = doc.select("body");
            makeMap(selectKeyList,mainWebMap,mainWeb,"body");

            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    iFinish.finish(getUrl(),mainWebMap);
                }
            });
        }
    }


    public void makeMap(ArrayList<String> selectKeyList,WebInfo mainWebMap,Elements e,String lastKey){
        for(String key:findKeyList){
            Elements insideWeb = e.select(lastKey+" "+key);
            if(insideWeb.size()!=0) {
//                Log.e("find key",key);
                if (!mainWebMap.containsKey(key)) {
                    mainWebMap.put(key,new HashSet<>());
                }
                for (Element element : insideWeb) {
                    if(element.children().size()!=0){
                        if(!selectKeyList.contains(lastKey+" "+key)) {
                            selectKeyList.add(lastKey + " " + key);
                            makeMap(selectKeyList,mainWebMap, e, lastKey + " " + key);
                        }
                    }else {
                        if(!element.text().isEmpty()){
                            mainWebMap.get(key).add(element.text());
                        }else {
                            String attr="";
                            for(Attribute a:element.attributes()){
                                attr = attr+a.getKey()+"="+a.getValue()+";";
                            }
                            mainWebMap.get(key).add(attr);
                        }
                    }
                }
            }
        }
    }
}
