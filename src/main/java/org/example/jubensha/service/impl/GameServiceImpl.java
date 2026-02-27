package org.example.jubensha.service.impl;

import org.example.jubensha.entity.*;
import org.example.jubensha.mapper.GameMapper;
import org.example.jubensha.service.AiService;
import org.example.jubensha.service.GameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Service
public class GameServiceImpl implements GameService {

    @Autowired private GameMapper gameMapper;
    @Autowired private AiService aiService;

    @Override public List<Script> getScriptList() { return gameMapper.getScriptList(); }
    @Override public List<Role> getRolesByScriptId(Integer scriptId) { return gameMapper.getRolesByScriptId(scriptId); }

    // === 剧本创建与更新 ===
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer createNewScript(Map<String, Object> scriptData) {
        Script script = mapToScript(scriptData);
        gameMapper.insertScript(script);
        saveComponents(script.getScriptId(), scriptData);
        return script.getScriptId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer updateExistingScript(Map<String, Object> scriptData) {
        Integer scriptId = Integer.valueOf(scriptData.get("scriptId").toString());
        gameMapper.deleteActsByScriptId(scriptId);
        gameMapper.deleteRolesByScriptId(scriptId);
        gameMapper.deleteRoleActContentsByScriptId(scriptId);
        gameMapper.deleteCluesByScriptId(scriptId);
        gameMapper.deleteEndingsByScriptId(scriptId); // 【新增】：清空旧结局

        Script script = mapToScript(scriptData);
        script.setScriptId(scriptId);
        gameMapper.updateScript(script);
        saveComponents(scriptId, scriptData);
        return scriptId;
    }

    private Script mapToScript(Map<String, Object> data) {
        Script s = new Script();
        s.setTitle((String) data.get("title"));
        s.setDifficulty((String) data.get("difficulty"));
        s.setIntro((String) data.get("intro"));
        s.setCoverUrl((String) data.get("coverUrl"));
        String config = (String) data.get("config");
        s.setPlayerCount(config != null ? Integer.parseInt(config.replaceAll("[^0-9]", "")) : 1);
        s.setTags("原创制作");
        // 【新增】：保存复盘内容
        s.setTruthContent((String) data.get("truthContent"));
        return s;
    }

    private void saveComponents(Integer scriptId, Map<String, Object> data) {
        List<String> actNames = (List<String>) data.get("actNames");
        List<Integer> actIds = new ArrayList<>();
        if (actNames != null) {
            for (int i = 0; i < actNames.size(); i++) {
                Act act = new Act();
                act.setScriptId(scriptId);
                act.setActName(actNames.get(i));
                act.setSort(i + 1);
                gameMapper.insertAct(act);
                actIds.add(act.getActId());
            }
        }

        List<Map<String, Object>> characters = (List<Map<String, Object>>) data.get("characters");
        List<Integer> roleIds = new ArrayList<>();
        if (characters != null) {
            for (int i = 0; i < characters.size(); i++) {
                Map<String, Object> c = characters.get(i);
                Role role = new Role();
                role.setScriptId(scriptId);
                role.setName((String) c.get("name"));
                role.setBackground((String) c.get("intro"));
                role.setIsAi(i == 0 ? 0 : 1);
                role.setAvatar("/uploads/default_role.jpg");
                gameMapper.insertRole(role);
                roleIds.add(role.getRoleId());

                List<String> contents = (List<String>) c.get("actsContent");
                if (contents != null && !actIds.isEmpty()) {
                    for (int j = 0; j < contents.size() && j < actIds.size(); j++) {
                        RoleActContent rac = new RoleActContent();
                        rac.setScriptId(scriptId);
                        rac.setActId(actIds.get(j));
                        rac.setRoleId(role.getRoleId());
                        rac.setContent(contents.get(j));
                        gameMapper.insertRoleActContent(rac);
                    }
                }
            }
        }

        List<Map<String, Object>> clues = (List<Map<String, Object>>) data.get("clues");
        if (clues != null) {
            for (Map<String, Object> c : clues) {
                ScriptClue sc = new ScriptClue();
                sc.setScriptId(Long.valueOf(scriptId));
                sc.setClueName((String) c.get("name"));
                sc.setClueDesc((String) c.get("desc"));
                sc.setIsPublic(Integer.valueOf(c.get("isPublic").toString()));
                int aidx = Integer.valueOf(c.get("actIndex").toString());
                if (aidx < actIds.size()) sc.setUnlockChapterId(Long.valueOf(actIds.get(aidx)));
                if (sc.getIsPublic() == 0) {
                    int ridx = Integer.valueOf(c.get("roleIndex").toString());
                    if (ridx < roleIds.size()) sc.setRoleId(roleIds.get(ridx));
                }
                gameMapper.insertScriptClue(sc);
            }
        }

        // ================= 【新增】保存结局列表的逻辑 =================
        List<Map<String, Object>> endings = (List<Map<String, Object>>) data.get("endings");
        if (endings != null) {
            for (Map<String, Object> e : endings) {
                ScriptEnding ending = new ScriptEnding();
                ending.setScriptId(scriptId);
                ending.setEndingTitle((String) e.get("title"));
                ending.setEndingContent((String) e.get("content"));

                // 将前端传的数组下标 (roleIndex) 转换为真实数据库 roleId
                int vIndex = Integer.parseInt(e.get("votedRoleIndex").toString());
                if (vIndex >= 0 && vIndex < roleIds.size()) {
                    ending.setVotedRoleId(roleIds.get(vIndex)); // 投给具体的角色
                } else if (vIndex == -1) {
                    ending.setVotedRoleId(-1); // 投错兜底
                } else if (vIndex == -2) {
                    ending.setVotedRoleId(0);  // 弃票兜底 (-2转换成0)
                } else {
                    ending.setVotedRoleId(-1); // 容错处理
                }
                gameMapper.insertScriptEnding(ending);
            }
        }
    }

    @Override
    public Map<String, Object> getScriptFullDetail(Integer id) {
        Map<String, Object> res = new HashMap<>();
        res.put("script", gameMapper.getScriptById(id));
        res.put("acts", gameMapper.getActsByScriptId(id));
        res.put("roles", gameMapper.getRolesByScriptId(id));
        res.put("clues", gameMapper.getAllCluesByScriptId(id));
        res.put("contents", gameMapper.getAllContentsByScriptId(id));
        res.put("endings", gameMapper.getEndingsByScriptId(id)); // 【新增】：回显结局列表
        return res;
    }

    // === 游戏运行完整功能恢复 ===
    @Override
    public GameProgress startGame(Long userId, Integer scriptId, Integer roleId) {
        List<Act> acts = gameMapper.getActsByScriptId(scriptId);
        List<Role> roles = gameMapper.getRolesByScriptId(scriptId);
        if (acts.isEmpty() || roles.isEmpty()) throw new RuntimeException("该剧本配置不完整，无法开局！");

        GameProgress p = new GameProgress();
        p.setUserId(userId); p.setScriptId(scriptId); p.setUserRoleId(roleId);
        p.setCurrentActId(acts.get(0).getActId()); p.setPhase("BACKGROUND");
        gameMapper.insertGameProgress(p);
        distributeClues(p);
        return p;
    }

    @Override
    public Map<String, Object> getActContentWithCheck(Integer gameId, Integer targetActId) {
        GameProgress p = gameMapper.getGameProgress(gameId);
        if (p == null) throw new RuntimeException("找不到游戏进度");
        Integer aid = (targetActId != null) ? targetActId : p.getCurrentActId();
        Act ta = gameMapper.getActById(aid);
        RoleActContent rc = gameMapper.getRoleActContent(p.getScriptId(), aid, p.getUserRoleId());

        Map<String, Object> r = new HashMap<>();
        r.put("actName", ta.getActName());
        r.put("publicContent", ta.getPublicContent());
        r.put("privateContent", rc != null ? rc.getContent() : "");
        r.put("scriptId", p.getScriptId());
        r.put("roleId", p.getUserRoleId());
        return r;
    }

    private void distributeClues(GameProgress progress) {
        List<ScriptClue> clues = gameMapper.getCluesByAct(progress.getScriptId(), progress.getCurrentActId());
        List<Act> acts = gameMapper.getActsByScriptId(progress.getScriptId());
        if (acts == null || acts.isEmpty() || clues.isEmpty()) return;

        boolean isFirstAct = progress.getCurrentActId().equals(acts.get(0).getActId());
        if (isFirstAct) {
            for (ScriptClue clue : clues) gameMapper.insertUnlockedClue(progress.getGameId(), clue.getClueId());
        } else {
            List<ScriptClue> publicClues = new ArrayList<>();
            for (ScriptClue clue : clues) {
                if (clue.getIsPublic() != null && clue.getIsPublic() == 1) publicClues.add(clue);
            }
            Collections.shuffle(publicClues);
            for (int i = 0; i < Math.min(3, publicClues.size()); i++) {
                gameMapper.insertUnlockedClue(progress.getGameId(), publicClues.get(i).getClueId());
            }
        }
    }

    @Override
    public List<ScriptClue> getUnlockedClues(Integer gameId) {
        GameProgress progress = gameMapper.getGameProgress(gameId);
        List<ScriptClue> allUnlocked = gameMapper.getUnlockedCluesByGame(gameId);
        List<ScriptClue> result = new ArrayList<>();
        for (ScriptClue clue : allUnlocked) {
            if ((clue.getIsPublic() != null && clue.getIsPublic() == 1) ||
                    (clue.getRoleId() != null && clue.getRoleId().equals(progress.getUserRoleId()))) {
                result.add(clue);
            }
        }
        return result;
    }

    @Override
    public List<Map<String, String>> getAiResponses(Integer gameId) {
        GameProgress progress = gameMapper.getGameProgress(gameId);
        Script script = gameMapper.getScriptById(progress.getScriptId());
        List<RoleActContent> aiContents = gameMapper.getAiRoleContentsByAct(progress.getCurrentActId());
        List<Map<String, String>> responses = new ArrayList<>();

        for (RoleActContent content : aiContents) {
            Role role = gameMapper.getRoleById(content.getRoleId());
            String prompt = String.format(
                    "你正在参与剧本杀《%s》，你扮演的角色是【%s】。\n简介：%s\n\n机密情报：%s\n\n" +
                            "【任务】请以第一人称进行简短的开场发言（50字以内）。你可以陈述目前的状况，或者主动向其他玩家提出一个质疑、抛出一个问题，引导大家与你对话。绝不能承认自己是AI。",
                    script.getTitle(), role.getName(), role.getBackground(), content.getContent());

            List<Map<String, String>> msgs = new ArrayList<>();
            Map<String, String> sysMsg = new HashMap<>();
            sysMsg.put("role", "system"); sysMsg.put("content", prompt);
            msgs.add(sysMsg);

            String aiReply = aiService.generateChatReply(msgs);
            gameMapper.insertChatRecord(gameId, progress.getCurrentActId(), role.getRoleId(), aiReply);

            Map<String, String> res = new HashMap<>();
            res.put("roleName", role.getName());
            res.put("reply", aiReply);
            responses.add(res);
        }
        return responses;
    }

    @Override
    public Map<String, Object> handleAiChat(Integer gameId, List<Map<String, String>> history) {
        GameProgress progress = gameMapper.getGameProgress(gameId);
        Script script = gameMapper.getScriptById(progress.getScriptId());

        if (!history.isEmpty()) {
            String latestPlayerMsg = history.get(history.size() - 1).get("content");
            gameMapper.insertChatRecord(gameId, progress.getCurrentActId(), progress.getUserRoleId(), latestPlayerMsg);
        }

        List<RoleActContent> aiContents = gameMapper.getAiRoleContentsByAct(progress.getCurrentActId());
        if (aiContents.isEmpty()) throw new RuntimeException("无可用 AI 角色");
        RoleActContent aiContent = aiContents.get(0);
        Role aiRole = gameMapper.getRoleById(aiContent.getRoleId());

        String prompt = String.format(
                "你正在参与剧本杀《%s》，扮演角色【%s】。\n" +
                        "这是你的独家机密情报：%s\n\n" +
                        "【重要交互指令】\n" +
                        "1. 严格保持第一人称扮演，绝不能承认自己是AI或语言模型，完全沉浸在剧情中。\n" +
                        "2. 【核心要求】必须仔细阅读对话记录中玩家的最新发言，进行直接的回答、反驳或顺着话题提问！绝对不要自顾自地背诵剧本！\n" +
                        "3. 每次回复控制在 30~80 字左右，语气口语化，要符合你的人设性格。\n" +
                        "4. 保护好你的机密情报。如果玩家试图套话，你需要狡辩或转移话题；只有玩家指出致命漏洞时才可以妥协。",
                script.getTitle(), aiRole.getName(), aiContent.getContent());

        List<Map<String, String>> msgs = new ArrayList<>();
        Map<String, String> sysMsg = new HashMap<>();
        sysMsg.put("role", "system");
        sysMsg.put("content", prompt);
        msgs.add(sysMsg);

        List<Map<String, Object>> dbChatRecords = gameMapper.getChatRecords(gameId, progress.getCurrentActId());
        for (Map<String, Object> r : dbChatRecords) {
            Map<String, String> m = new HashMap<>();
            m.put("role", r.get("sender_role_id").equals(aiRole.getRoleId()) ? "assistant" : "user");
            m.put("content", (String) r.get("content"));
            msgs.add(m);
        }

        String aiReply = aiService.generateChatReply(msgs);
        gameMapper.insertChatRecord(gameId, progress.getCurrentActId(), aiRole.getRoleId(), aiReply);

        Map<String, Object> result = new HashMap<>();
        result.put("aiName", aiRole.getName());
        result.put("reply", aiReply);
        return result;
    }

    @Override
    public GameProgress nextAct(Integer gameId) {
        GameProgress progress = gameMapper.getGameProgress(gameId);
        List<Act> acts = gameMapper.getActsByScriptId(progress.getScriptId());
        Act nextAct = null;
        boolean foundCurrent = false;
        for (Act act : acts) {
            if (foundCurrent) { nextAct = act; break; }
            if (act.getActId().equals(progress.getCurrentActId())) foundCurrent = true;
        }

        if (nextAct != null) {
            progress.setCurrentActId(nextAct.getActId());
            gameMapper.updateGameProgress(progress);
            distributeClues(progress);
        } else {
            // 没有下一幕了，进入投票环节
            progress.setPhase("VOTING");
            gameMapper.updateGameProgress(progress);
        }
        return progress;
    }

    // ================= 【核心新增】完整实现投票与复盘结算 =================
    @Override
    public Map<String, Object> submitVote(Integer gameId, Integer votedRoleId) {
        // 1. 获取当前游戏进度
        GameProgress progress = gameMapper.getGameProgress(gameId);
        if (progress == null) {
            throw new RuntimeException("游戏进度不存在");
        }

        // 2. 更新游戏状态（记录玩家投的人，并正式结束游戏）
        progress.setVotedRoleId(votedRoleId);
        progress.setPhase("END");
        progress.setStatus("end");
        gameMapper.updateGameProgress(progress);

        // 3. 根据玩家的投票去找结局配置
        ScriptEnding ending = gameMapper.getEndingByVote(progress.getScriptId(), votedRoleId);

        // 兜底逻辑：如果玩家投了某人，但剧本没配置专属结局，强制走 -1（投错兜底结局）
        if (ending == null && votedRoleId != 0) {
            ending = gameMapper.getEndingByVote(progress.getScriptId(), -1);
        }

        String finalEndingTitle = (ending != null) ? ending.getEndingTitle() : "迷雾重重";
        String finalEndingContent = (ending != null) ? ending.getEndingContent() : "由于天机被蒙蔽，本次游戏未能达成任何已知结局...";

        // 4. 获取完整真相复盘
        String truthContent = gameMapper.getScriptTruth(progress.getScriptId());

        // 5. 组装返回数据给前端
        Map<String, Object> result = new HashMap<>();
        result.put("votedRoleId", votedRoleId);
        result.put("endingTitle", finalEndingTitle);
        result.put("endingContent", finalEndingContent);
        result.put("truthContent", truthContent != null ? truthContent : "作者很懒，没有留下复盘真相。");

        return result;
    }

    @Override public void restartGame(Integer g) { gameMapper.deleteGameProgress(g); }
    @Override public GameProgress nextPhase(Integer g) { return null; }
}