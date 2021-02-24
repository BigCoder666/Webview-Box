# Webview-Box
visit website and get info from the webpage


webBoxView = findViewById(R.id.web_box);
        webBoxView.loadUrl(WebBoxActivity.this,"https://www.jd.com/",
                new WebBoxView.IFinish() {
                    @Override
                    public void finish(String url, WebInfo webInfo) {
                        String info =JSON.toJSONString(webInfo);
                        Log.e("info", info);
                    }
                });
