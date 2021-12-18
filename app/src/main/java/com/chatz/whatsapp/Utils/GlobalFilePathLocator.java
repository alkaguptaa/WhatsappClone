package com.chatz.whatsapp.Utils;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.text.TextUtils;
import android.webkit.MimeTypeMap;

import com.google.android.gms.common.util.IOUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @Author jaydip sardhara
 */
public class GlobalFilePathLocator {
    public static String getPath(final Context context, final Uri uri) {
        try {
            // DocumentProvider
            if (DocumentsContract.isDocumentUri(context, uri)) {

                // ExternalStorageProvider
                if (isExternalStorageDocument(uri)) {
                    final String docId = DocumentsContract.getDocumentId(uri);
                    final String[] split = docId.split(":");
                    final String type = split[0];

                    if ("primary".equalsIgnoreCase(type)) {
                        return Environment.getExternalStorageDirectory() + "/"
                                + split[1];
                    }
                }
                // DownloadsProvider
                else if (isDownloadsDocument(uri)) {
                    final String id = DocumentsContract.getDocumentId(uri);
                    final Uri contentUri = ContentUris.withAppendedId(
                            Uri.parse("content://downloads/public_downloads"),
                            Long.valueOf(id));

                    return getDataColumn(context, contentUri, null, null);
                }
                // MediaProvider
                else if (isMediaDocument(uri)) {
                    final String docId = DocumentsContract.getDocumentId(uri);
                    final String[] split = docId.split(":");
                    final String type = split[0];

                    Uri contentUri = null;
                    if ("image".equals(type)) {
                        contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                    } else if ("video".equals(type)) {
                        contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                    } else if ("audio".equals(type)) {
                        contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                    }

                    final String selection = "_id=?";
                    final String[] selectionArgs = new String[]{split[1]};

                    return getDataColumn(context, contentUri, selection,
                            selectionArgs);
                }
                //From Google photos
                else if (isGooglePhotosUri(uri)) {
                    // System.out.println("from helper 4==>");
                }
            }
            // MediaStore (and general)
            else if ("content".equalsIgnoreCase(uri.getScheme())) {
                // Return the remote address
                String mimeType = getMimeTypeFromUri(uri, context);
                if (isGooglePhotosUri(uri)) {
                    return uri.getLastPathSegment();
                } else if (isNewGooglePhotosUri(uri) && mimeType.startsWith("image/")) {
                    return getImageUrlWithAuthority(context, uri);
                }

                return getDataColumn(context, uri, null, null);
            }
            // File
            else if ("file".equalsIgnoreCase(uri.getScheme())) {
                //  System.out.println("from helper 8==>");
                return uri.getPath();
            } else {
                // System.out.println("from helper 9==>");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getImageUrlWithAuthority(Context context, Uri uri) {
        try {
            InputStream is = null;
            if (uri.getAuthority() != null) {
                try {
                    is = context.getContentResolver().openInputStream(uri);
                    Bitmap bmp = BitmapFactory.decodeStream(is);

                    Uri newUri = writeToTempImageAndGetPathUri(context, bmp);
                    //String newPath=getPath(context,newUri);
                    return getDataColumn(context, newUri, null, null);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (is != null) {
                            is.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Uri writeToTempImageAndGetPathUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

    public static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {

        Cursor cursor = null;
        final String column = MediaStore.Images.Media.DATA;
        final String[] projection = {column};
        String path = null;
        try {
            cursor = context.getContentResolver().query(uri, projection,
                    selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                final int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return path;
    }


    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri
                .getAuthority());
    }


    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri
                .getAuthority());
    }

    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri
                .getAuthority());
    }


    public static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri
                .getAuthority());
    }

    public static boolean isGooglePhotosUri2(Uri uri) {
        return "com.google.android.apps.photos.contentprovider".equals(uri
                .getAuthority());
    }

    public static boolean isNewGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.contentprovider".equals(uri.getAuthority());
    }


    public static String getMimeTypeFromUri(Uri uri, Context context) {
        try {
            ContentResolver cR = context.getContentResolver();
            MimeTypeMap mime = MimeTypeMap.getSingleton();
            String type = mime.getExtensionFromMimeType(cR.getType(uri));
            return cR.getType(uri);


        } catch (Exception e) {
            e.printStackTrace();
        }

        return "";

    }

    public static String getSharedFileData(Uri sharedUri, Context context) {
        InputStream inputStream = null;
        try {

            Cursor returnCursor = context.getContentResolver().query(sharedUri, null, null, null, null);
            /*
             * Get the column indexes of the data in the Cursor,
             * move to the first row in the Cursor, get the data,
             * and display it.
             */
            assert returnCursor != null;
            int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);

            /*int sizeIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE);
            int data = returnCursor.getColumnIndex(MediaStore.Images.Media.DATA);*/
            returnCursor.moveToFirst();
            String fileName = returnCursor.getString(nameIndex);
            inputStream = context.getContentResolver().openInputStream(sharedUri);
            if (inputStream != null) {
                File tempDir = context.getCacheDir();
                File tempFile = new File(tempDir, fileName);
                boolean status = createFileFromInputStream(inputStream, tempFile);
                if (status) {
                    return tempFile.getAbsolutePath();
                }
            } else {
                return null;
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return null;
    }

    public static boolean createFileFromInputStream(InputStream in, File destFile) {
        try {
            OutputStream out = null;
            try {
                out = new FileOutputStream(destFile);
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                // Ensure that the InputStreams are closed even if there's an exception.
                try {
                    if (out != null) {
                        out.close();
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return destFile.length() > 0;
    }


    //input stream

    public static String getFilePathFromInputStreamURI(Context context, Uri contentUri) {
        //copy file and send new file path
        try {
            String fileName = getFileName(contentUri);
            if (!TextUtils.isEmpty(fileName)) {
                File copyFile = new File(Environment.getExternalStorageDirectory().getPath() + File.separator + fileName);
                if (!copyFile.exists()) {
                    copyFile.createNewFile();
                }
                //copy(context, contentUri, copyFile);
                InputStream input = context.getContentResolver().openInputStream(contentUri);
                try (OutputStream output = new FileOutputStream(copyFile)) {
                    byte[] buffer = new byte[4 * 1024]; // or other buffer size
                    int read;

                    while ((read = input.read(buffer)) != -1) {
                        output.write(buffer, 0, read);
                    }

                    output.flush();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (input != null) {
                        input.close();
                    }
                }

                return copyFile.getAbsolutePath();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getFileName(Uri uri) {
        if (uri == null) return null;
        String fileName = null;
        String path = uri.getPath();
        int cut = path.lastIndexOf('/');
        if (cut != -1) {
            fileName = path.substring(cut + 1);
        }
        return fileName;
    }

    public static void copy(Context context, Uri srcUri, File dstFile) {
        InputStream inputStream = null;
        try {
            inputStream = context.getContentResolver().openInputStream(srcUri);
            if (inputStream == null) return;
            OutputStream outputStream = new FileOutputStream(dstFile);
            IOUtils.copyStream(inputStream, outputStream);
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
