#include <a_samp>

#define LV_SERVER_NAME "Las Venturas RP"
#define LV_SERVER_IP "142.132.203.47:21299"
#define COLOR_LV 0xD9A441FF
#define COLOR_INFO 0xB9C7D6FF

new bool:gRulesAccepted[MAX_PLAYERS];
new bool:gVoiceEnabled[MAX_PLAYERS];
new bool:gParachuteSpawn[MAX_PLAYERS];

public OnGameModeInit()
{
    SetGameModeText("Arabic Roleplay | Las Venturas");
    ShowPlayerMarkers(PLAYER_MARKERS_MODE_OFF);
    ShowNameTags(1);
    UsePlayerPedAnims();
    DisableInteriorEnterExits();
    EnableStuntBonusForAll(0);
    SetWorldTime(21);
    print("[Las Venturas RP] Game mode loaded");
    return 1;
}

public OnPlayerConnect(playerid)
{
    gRulesAccepted[playerid] = false;
    gVoiceEnabled[playerid] = true;
    gParachuteSpawn[playerid] = false;
    SendClientMessage(playerid, COLOR_LV, "Las Venturas RP | أهلاً بك في المدينة.");
    SendClientMessage(playerid, COLOR_INFO, "اكتب /rules لقراءة القوانين ثم /accept للمتابعة.");
    return 1;
}

public OnPlayerDisconnect(playerid, reason)
{
    gRulesAccepted[playerid] = false;
    return 1;
}

public OnPlayerSpawn(playerid)
{
    if (!gRulesAccepted[playerid])
    {
        TogglePlayerControllable(playerid, 0);
        SendClientMessage(playerid, COLOR_INFO, "يجب قبول قوانين الرول بلاي قبل اللعب.");
    }
    else if (gParachuteSpawn[playerid])
    {
        GivePlayerWeapon(playerid, 46, 1);
        SetPlayerPos(playerid, 1685.0, -2280.0, 160.0);
        SetPlayerInterior(playerid, 0);
        TogglePlayerControllable(playerid, 1);
    }
    return 1;
}

public OnPlayerCommandText(playerid, cmdtext[])
{
    if (!strcmp(cmdtext, "/rules", true))
    {
        ShowPlayerDialog(playerid, 1000, DIALOG_STYLE_MSGBOX, "قوانين Las Venturas RP",
            "1. احترم اللاعبين والإدارة.\n2. ممنوع الغش أو استغلال الثغرات.\n3. التزم بالرول بلاي داخل المدينة.\n4. يمنع نشر محتوى مخالف.\n\nاكتب /accept للموافقة.", "موافق", "إغلاق");
        return 1;
    }
    if (!strcmp(cmdtext, "/accept", true))
    {
        gRulesAccepted[playerid] = true;
        TogglePlayerControllable(playerid, 1);
        SendClientMessage(playerid, COLOR_LV, "تم قبول القوانين. استمتع باللعب!");
        return 1;
    }
    if (!strcmp(cmdtext, "/eye", true) || !strcmp(cmdtext, "/عين", true))
    {
        ShowPlayerDialog(playerid, 1001, DIALOG_STYLE_LIST, "قائمة العين",
            "فتح الموبايل\nفتح المخزون\nالرقصات\nالخريطة\nالأنميشن\nإعدادات الأزرار", "اختيار", "إغلاق");
        return 1;
    }
    if (!strcmp(cmdtext, "/parachute", true))
    {
        gParachuteSpawn[playerid] = true;
        SpawnPlayer(playerid);
        SendClientMessage(playerid, COLOR_INFO, "تم اختيار النزول بالمظلة.");
        return 1;
    }
    if (!strcmp(cmdtext, "/voice", true))
    {
        gVoiceEnabled[playerid] = !gVoiceEnabled[playerid];
        SendClientMessage(playerid, COLOR_INFO, gVoiceEnabled[playerid] ? "تم تشغيل المايك." : "تم إيقاف المايك.");
        return 1;
    }
    if (!gRulesAccepted[playerid])
    {
        SendClientMessage(playerid, COLOR_INFO, "اقرأ القوانين واكتب /accept أولاً.");
        return 1;
    }
    return 0;
}

public OnDialogResponse(playerid, dialogid, response, listitem, inputtext[])
{
    if (!response) return 1;
    if (dialogid == 1001)
    {
        switch (listitem)
        {
            case 0: SendClientMessage(playerid, COLOR_INFO, "واجهة الموبايل جاهزة للربط مع filterscript الموبايل.");
            case 1: SendClientMessage(playerid, COLOR_INFO, "واجهة المخزون جاهزة للربط مع نظام العناصر.");
            case 2: SendClientMessage(playerid, COLOR_INFO, "واجهة الرقصات جاهزة للربط مع animations.");
            case 3: SendClientMessage(playerid, COLOR_INFO, "الخريطة المخصصة: Las Venturas Night.");
            case 4: SendClientMessage(playerid, COLOR_INFO, "واجهة الأنميشن جاهزة.");
            case 5: SendClientMessage(playerid, COLOR_INFO, "عدّل أزرار التحكم من إعدادات اللانشر.");
        }
    }
    return 1;
}
