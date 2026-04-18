package ru.eto.ya.m.generated.ispitaniya.aoh2de.mod;


import age.of.civilizations2.jakowski.lukasz.CFG;
import age.of.civilizations2.jakowski.lukasz.Civilization;
import age.of.civilizations2.jakowski.lukasz.Files.FileManager;
import age.of.civilizations2.jakowski.lukasz.FormableCivs_GameData;
import age.of.civilizations2.jakowski.lukasz.Game_Scenarios;
import age.of.civilizations2.jakowski.lukasz.MapA.Challenge.Challenge;
import age.of.civilizations2.jakowski.lukasz.MapA.Challenge.ChallengesManager;
import age.of.civilizations2.jakowski.lukasz.MenuE_HoverP.MEHover_2E;
import age.of.civilizations2.jakowski.lukasz.MenuE_HoverP.ME_Hover_2Type_Text_Big;
import age.of.civilizations2.jakowski.lukasz.Messages.Gift.R.Menu_Main;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.GdxRuntimeException;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Mixin(ChallengesManager.class)
public abstract class MainMixin {
//    @Shadow private static List<Challenge> challengeList;

    @Inject(method = "loadChallenges", at=@At("RETURN"))
    private static void addRandomChallenge(CallbackInfo ci){
        try {
            List<String> tempTags = new ArrayList<>();
            Consumer<Supplier<FileHandle>> loadTags = getSupplierConsumer(tempTags);

            if (CFG.readLocalFiles()) {
                loadTags.accept(() -> FileManager.loadFile("map/" + CFG.map.getFileActiveMapPath() + "formable_civs/Age_of_Civilizations"));
                loadTags.accept(() -> Gdx.files.internal("map/" + CFG.map.getFileActiveMapPath() + "formable_civs/Age_of_Civilizations"));
                loadTags.accept(() -> Gdx.files.local("map/" + CFG.map.getFileActiveMapPath() + "formable_civs/Age_of_Civilizations"));
            } else {
                loadTags.accept(() -> FileManager.loadFile("map/" + CFG.map.getFileActiveMapPath() + "formable_civs/Age_of_Civilizations"));
            }

            Random random = new Random();
            String randomTag = tempTags.get(random.nextInt(tempTags.size()));
            CFG.loadFormableCiv_GameData(randomTag);
            String randomCivTag = CFG.formableCivs_GameData.getClaimant(random.nextInt(CFG.formableCivs_GameData.getClaimantsSize()));

            List<Integer> scenariosWithThis = new ArrayList<>();
            Map<Integer, String> scenarioCivTags = new HashMap<>();
            for (int i = 0; i < Game_Scenarios.SCENARIOS_SIZE; i++) {
                CFG.core.setScenarioID(i);
                List<Civilization> civilizations = CFG.core.getGameScenars().loadCivilizations(false);
                for (Civilization civ : civilizations) {
                    if (civ.getCivTag().matches(randomCivTag + ".*")) {
                        scenariosWithThis.add(i);
                        scenarioCivTags.put(i, civ.getCivTag());
                        break;
                    }
                }
            }
            int scenar = scenariosWithThis.get(random.nextInt(scenariosWithThis.size()));

            Challenge nChallenge = new Challenge();
            nChallenge.ID = "0";
            nChallenge.PLAY_AS = scenarioCivTags.get(scenar);
            nChallenge.FORM_TAG = randomTag;
            nChallenge.DESC = "Возглавь страну и приведи её к величию!";
            nChallenge.SCENARIO_TAG = CFG.core.getGameScenars().getScenarioTagID(scenar);
            nChallenge.PROVINCES = CFG.formableCivs_GameData.getProvincesSize();
            nChallenge.PROVINCES_FORM = 2000;
            nChallenge.ADD_CIV_PROVINCES = null;
            ChallengesManager.challengeList.add(nChallenge);

            CFG.formableCivs_GameData.clearProvinces();
            CFG.formableCivs_GameData = null;
        } catch (IndexOutOfBoundsException e){
            return;
        }
    }

    private static Consumer<Supplier<FileHandle>> getSupplierConsumer(List<String> tempTags) {
        Set<String> uniqueTags = new HashSet<>();
        Consumer<Supplier<FileHandle>> loadTags = (fileSupplier) -> {
            try {
                FileHandle file = fileSupplier.get();
                String content = file.readString();
                String[] tags = content.split(";");
                for (String tag : tags) {
                    if (uniqueTags.add(tag)) {
                        tempTags.add(tag);
                    }
                }
            } catch (GdxRuntimeException e) {
                System.err.println("Failed to load tags from: " + fileSupplier);
            }
        };
        return loadTags;
    }
}
