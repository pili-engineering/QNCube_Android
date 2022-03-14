package com.qiniudemo.baseapp.been;


import java.io.Serializable;

public class AppConfigModel implements Serializable{

    private WelCome welcome;

    public WelCome getWelcome() {
        return welcome;
    }

    public void setWelcome(WelCome welcome) {
        this.welcome = welcome;
    }

    public static class WelCome implements Serializable{
        /**
         * url : culpa eiusmod
         * image : https://www-static.qbox.me/_next/static/media/qrcode.90ffc3c61c01a49f433945a0ea474c8e.png
         */
        private String url;
        private String image;

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getImage() {
            return image;
        }

        public void setImage(String image) {
            this.image = image;
        }
    }

}
