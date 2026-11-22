# Guftagu Android — Firebase starter

یہ Android Studio کا ابتدائی پروجیکٹ ہے۔ اس میں Firebase Phone Authentication اور Firestore پر بنیادی real-time text chat شامل ہے۔

## APK ابھی کیوں شامل نہیں؟
اس ماحول میں Android SDK/Gradle build setup موجود نہیں، اور Firebase project کی `google-services.json` فائل بھی آپ کے Firebase اکاؤنٹ سے حاصل ہونی ہے۔ اس لیے یہاں سے ایک مکمل installable APK بنانا ممکن نہیں۔ یہ source project ہے جسے Android Studio میں configure کر کے APK بنایا جا سکتا ہے۔

## Firebase setup
1. https://console.firebase.google.com/ پر نیا Firebase project بنائیں۔
2. Android app شامل کریں، آپ کی اپلوڈ کی ہوئی Firebase فائل کے مطابق package name `Guftugu.app.com` رکھیں۔
3. `google-services.json` آپ کی فراہم کردہ فائل سے `app/` فولڈر میں شامل کر دی گئی ہے۔
4. Authentication → Sign-in method میں Phone فعال کریں۔ SMS region policy میں اپنے مطلوبہ ممالک allow کریں۔
5. Firestore Database بنائیں اور اس project کے `firestore.rules` کے قواعد publish کریں۔
6. (Media uploads کے لیے) Firebase Storage فعال کریں؛ موجودہ starter میں media UI ابھی شامل نہیں۔
7. Android Studio میں فولڈر کھولیں، Gradle sync کریں، پھر Build → Build APK(s) منتخب کریں۔

## موجودہ حدود
- OTP login Firebase پر ہے (Firebase Console میں Phone provider configure کرنا ضروری ہے)۔
- ایک صارف دوسرے سے اس کے Firebase UID کے ذریعے chat کھولتا ہے؛ ابھی phone-number contact discovery/user directory شامل نہیں۔
- Text messages Firestore listener کے ذریعے real-time آتے ہیں۔
- Voice/video calls، group chat، profile photos، push notifications، media upload اور مکمل online/last-seen نظام اس starter میں ابھی implement نہیں ہیں۔
- Production launch سے پہلے chat membership پر مبنی سخت Firestore security rules، abuse controls، privacy policy اور testing ضروری ہیں۔

## اہم
`firestore.rules` ایک ابتدائی مثال ہے، production کے لیے کافی نہیں۔ بہتر rules کے لیے chat participants کی الگ membership list بنائیں اور ہر read/write پر participant membership validate کریں۔
