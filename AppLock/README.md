# Focus Lock

eFootball, PUBG Mobile va Mobile Legends kabi tanlangan o'yinlarni kod bilan
bloklaydigan Android ilova. Kod avtomatik generatsiya qilinmaydi — uni
do'stingiz o'zi qo'lda o'rnatadi, shunday qilib o'yinni ochish uchun unga
murojaat qilishga to'g'ri keladi.

## GitHub'ga yuklash va APK olish

1. Ushbu papkadagi hamma narsani yangi GitHub repositoriyaga yuklang:
   ```
   git init
   git add .
   git commit -m "Focus Lock v1"
   git branch -M main
   git remote add origin <repo-url>
   git push -u origin main
   ```
2. GitHub'da **Actions** bo'limiga o'ting — "Build APK" workflow avtomatik
   ishga tushadi (push qilinganda yoki qo'lda "Run workflow" orqali).
3. Workflow tugagach, **Artifacts** bo'limidan `focus-lock-debug-apk` faylini
   yuklab oling — ichida `app-debug.apk` bo'ladi.

## Telefonga o'rnatish

1. APK faylni telefonga ko'chiring (Telegram, USB, Google Drive va h.k.).
2. Sozlamalar → Xavfsizlik → "Noma'lum manbalardan o'rnatish"ni yoqing
   (faqat shu APK uchun so'raladi, brauzer/fayl menejer ilovasiga ruxsat bering).
3. APK faylni oching, o'rnating.

## Ilovani sozlash (do'stingiz bilan birga, birinchi marta)

1. Ilovani oching — birinchi marta kod o'rnatish ekrani ochiladi.
2. Do'stingiz shu yerda **o'zi xohlagan kodni** kiritadi va tasdiqlaydi.
3. Asosiy ekranga qaytib, ikkita ruxsatni yoqing:
   - **Accessibility xizmati** — "Ruxsatni yoqish" tugmasi orqali sozlamalarga
     o'tiladi, ro'yxatdan "Focus Lock"ni topib yoqing.
   - **Boshqa ilovalar ustidan chiqish** (overlay) — agar so'ralsa, xuddi
     shunday yoqing.
4. "+ Ilova qo'shish" tugmasini bosing, kodni kiriting (do'stingiz kiritadi),
   so'ng eFootball, PUBG Mobile, Mobile Legends'ni belgilang.

Shundan keyin bu uchta o'yinni ochishga harakat qilsangiz, kod so'raladigan
ekran chiqadi. Kodni faqat do'stingiz biladi.

## Eslatma

- Bu ilova Android'ning maxsus "Device Admin" huquqisiz ishlaydi, shuning
  uchun uni Sozlamalar → Ilovalar orqali oddiy usulda o'chirib tashlash
  texnik jihatdan mumkin. Bunday urinish tarixga yoziladi (Accessibility
  xizmati o'chirilgani sifatida), do'stingiz buni Tarix bo'limida ko'radi.
- Ikkalangiz ham bir xil APK'ni o'z telefonlaringizga o'rnatib, bir-biringiz
  uchun kod qo'yishingiz mumkin.
