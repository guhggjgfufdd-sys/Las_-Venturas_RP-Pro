# Las Venturas RP — SA-MP Mobile

هذه النسخة مبنية على فرع **SA-MP Mobile GTA-2.10**، وهو الفرع الذي يوفر مكتبات **ARM32 (`armeabi-v7a`) وARM64 (`arm64-v8a`)**. تمت إضافة إعدادات Las Venturas RP، تسجيل السيرفر الافتراضي، رابط المجتمع، وقالب Pawn ابتدائي قابل للتوسعة.

## إعداد السيرفر

| الحقل | القيمة |
|---|---|
| الاسم | Las Venturas RP |
| العنوان | `142.132.203.47:21299` |
| Discord | [discord.gg/eZFKQ83ke](https://discord.gg/eZFKQ83ke) |
| كاش GTA | [2.11.gtasa.zip](https://github.com/guhggjgfufdd-sys/SAMP-Mobile-Launcher-RN/releases/download/v1.0/2.11.gtasa.zip) |
| مسار الكاش | `/storage/emulated/0/GTA/` |

إعداد اللانشر القابل للقراءة موجود في `app/src/main/assets/las_venturas/launcher.json`. وضعت خانة `sha256` كـ `VERIFY_BEFORE_DISTRIBUTION` لأن رابط GitHub الحالي لا يوفر قيمة تحقق منشورة؛ يجب حسابها بعد تنزيل الملف قبل توزيعه على اللاعبين.

## الميزات المضافة في هذه الدفعة

* دعم بناء ARM32 وARM64 عبر إعدادات فرع GTA-2.10 الموجودة أصلًا.
* إضافة Las Venturas RP تلقائيًا إلى قائمة السيرفرات المفضلة.
* تحديث اسم التطبيق إلى Las Venturas RP.
* تحديث رابط Discord إلى رابط المجتمع المقدم.
* ملف مواصفات يصف دعم المايك، الإعدادات القابلة للتعديل، قائمة العين، الخريطة المخصصة، قبول القوانين، والمظلة.
* قالب سيرفر Pawn في `server/las_venturas_rp.pwn` يتضمن `/rules` و`/accept` و`/eye` و`/parachute` و`/voice`، وهو نقطة بداية لربط inventory/mobile/animations عبر filterscripts.

## البناء

```bash
./gradlew assembleDebug
```

الناتج المتوقع يكون داخل `app/build/outputs/apk/`. يجب تنفيذ البناء على Android Studio/SDK يحتوي على NDK `26.2.11394342` كما هو محدد في المشروع.

## قالب السكربت

الملف `server/las_venturas_rp.pwn` ليس نظام رول بلاي كاملًا جاهزًا للإنتاج؛ هو قالب نظيف وآمن للبدء. أنظمة الحسابات، الوظائف، الاقتصاد، المركبات، المخزون، الموبايل، الصوت، الخريطة، والأنميشن تحتاج filterscripts أو plugins متوافقة مع إصدار خادم SA-MP المستخدم، ولا يمكن ضمان تشغيل أي سكربت عشوائي بلا تعديل بسبب اختلاف الـ callbacks والـ natives والـ plugins.

## ملاحظة مهمة عن إصدار 2.11

فرع GTA-2.11 في المشروع الأصلي **ARM64 فقط**، بينما طلب ARM32 وARM64 يتطلب فرع GTA-2.10. لذلك لا ينبغي خلط مكتبات كاش 2.11 مع عميل 2.10 إلا بعد اختبار كامل على جهاز حقيقي. كذلك يجب توزيع ملفات اللعبة والكاش فقط إذا كانت لديك حقوق توزيعها.

## الترخيص والمسؤولية

المشروع لأغراض تعليمية وبحثية، وSA-MP وGrand Theft Auto علامات تجارية لأصحابها. احصل على نسخة اللعبة الأصلية، وراجع تراخيص أي plugins أو filterscripts قبل نشرها.
