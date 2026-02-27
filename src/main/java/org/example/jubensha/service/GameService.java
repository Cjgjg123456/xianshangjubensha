package org.example.jubensha.service;

import org.example.jubensha.entity.*;
import java.util.List;
import java.util.Map;

public interface GameService {
    List<Script> getScriptList();
    List<Role> getRolesByScriptId(Integer scriptId);
    GameProgress startGame(Long userId, Integer scriptId, Integer roleId);
    Map<String, Object> getActContentWithCheck(Integer gameId, Integer targetActId);
    List<Map<String, String>> getAiResponses(Integer gameId);
    GameProgress nextAct(Integer gameId);
    void restartGame(Integer gameId);
    Map<String, Object> handleAiChat(Integer gameId, List<Map<String, String>> history);

    // 创作者空间相关
    Integer createNewScript(Map<String, Object> scriptData);
    Integer updateExistingScript(Map<String, Object> scriptData);
    Map<String, Object> getScriptFullDetail(Integer scriptId);

    GameProgress nextPhase(Integer gameId);
    List<ScriptClue> getUnlockedClues(Integer gameId);
    Map<String, Object> submitVote(Integer gameId, Integer voteRoleId);
}