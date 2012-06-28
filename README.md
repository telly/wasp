Wasp - Bitmap Network Utilities for Android
===========================================

This library contains some helpers used to download images asynchronously and get their bitmap.
It was almost entirely written by [Evelio Tarazona][1] and hopefully he won't get upset after seeing this.

I had to move this code here since I have been using it everywhere and don't want to keep repeating it.

###Examples

This will load the image to the image view asynchronously

```java
ImageView imageView = (ImageView) findViewById(R.id.image_view_id);
BitmapHelper bh = BitmapHelper.getInstance();
Bitmap bitmap = bh.getBitmap(imageUrl);
if (BitmapUtils.isBitmapValid(bitmap)) {
    imageView.setImageBitmap(bitmap);
} else {
    imageView.setTag(imageUrl); // this is mandatory!
    BitmapObserver observer = new BitmapObserver(imageView, imageUrl, new Handler());
    bh.registerBitmapObserver(this, imageUrl, observer);
}
```

If you are not using an `ImageView` you better use `CallbackBitmapObserver` class:

```java
CallbackBitmapObserver avatarObserver = new CallbackBitmapObserver(
        new CallbackBitmapObserver.BitmapCallback() {
            @Override
            public boolean stillNeedsUrl(String url) {
                return true;
            }

            @Override
            public void receiveBitmap(Bitmap bitmap) {
                // do something with the bitmap
            }
        }, imageUrl, new Handler());

BitmapHelper bh = BitmapHelper.getInstance();
Bitmap bitmap = bh.getBitmap(imageUrl);
if (BitmapUtils.isBitmapValid(bitmap)) {
    // do something with the bitmap
} else {
    bh.registerBitmapObserver(this, imageUrl, avatarObserver);
}
```

  [1]: https://github.com/eveliotc