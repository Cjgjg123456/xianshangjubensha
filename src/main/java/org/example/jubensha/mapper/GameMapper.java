package org.example.jubensha.mapper;

import org.apache.ibatis.annotations.*;
import org.example.jubensha.entity.*;
import java.util.List;
import java.util.Map;

@Mapper
public interface GameMapper {

    @Select("SELECT * FROM script ORDER BY script_id DESC")
    List<Script> getScriptList();

    @Select("SELECT * FROM script WHERE script_id = #{scriptId}")
    Script getScriptById(Integer scriptId);

    @Select("SELECT * FROM role WHERE script_id = #{scriptId}")
    List<Role> getRolesByScriptId(Integer scriptId);

    @Select("SELECT * FROM act WHERE script_id = #{scriptId} ORDER BY sort ASC")
    List<Act> getActsByScriptId(Integer scriptId);

    @Select("SELECT * FROM act WHERE act_id = #{actId}")
    Act getActById(Integer actId);

    @Select("SELECT * FROM role_act_content WHERE script_id = #{scriptId}")
    List<RoleActContent> getAllContentsByScriptId(Integer scriptId);

    @Select("SELECT * FROM script_clue WHERE script_id = #{scriptId}")
    List<ScriptClue> getAllCluesByScriptId(Integer scriptId);

    @Select("SELECT * FROM role_act_content WHERE script_id = #{scriptId} AND act_id = #{actId} AND role_id = #{roleId}")
    RoleActContent getRoleActContent(@Param("scriptId") Integer scriptId, @Param("actId") Integer actId, @Param("roleId") Integer roleId);

    // === 剧本维护 (修改：增加了 truth_content) ===
    @Insert("INSERT INTO script (title, intro, player_count, difficulty, cover_url, tags, truth_content) VALUES (#{title}, #{intro}, #{playerCount}, #{difficulty}, #{coverUrl}, #{tags}, #{truthContent})")
    @Options(useGeneratedKeys = true, keyProperty = "scriptId")
    void insertScript(Script script);

    @Update("UPDATE script SET title=#{title}, intro=#{intro}, player_count=#{playerCount}, difficulty=#{difficulty}, cover_url=#{coverUrl}, tags=#{tags}, truth_content=#{truthContent} WHERE script_id=#{scriptId}")
    void updateScript(Script script);

    @Insert("INSERT INTO act (script_id, act_name, sort) VALUES (#{scriptId}, #{actName}, #{sort})")
    @Options(useGeneratedKeys = true, keyProperty = "actId")
    void insertAct(Act act);

    @Insert("INSERT INTO role (script_id, name, avatar, background, is_ai) VALUES (#{scriptId}, #{name}, #{avatar}, #{background}, #{isAi})")
    @Options(useGeneratedKeys = true, keyProperty = "roleId")
    void insertRole(Role role);

    @Insert("INSERT INTO role_act_content (script_id, act_id, role_id, content) VALUES (#{scriptId}, #{actId}, #{roleId}, #{content})")
    void insertRoleActContent(RoleActContent content);

    @Insert("INSERT INTO script_clue (script_id, clue_name, clue_desc, is_public, role_id, unlock_chapter_id) VALUES (#{scriptId}, #{clueName}, #{clueDesc}, #{isPublic}, #{roleId}, #{unlockChapterId})")
    void insertScriptClue(ScriptClue clue);

    @Delete("DELETE FROM act WHERE script_id = #{scriptId}") void deleteActsByScriptId(Integer scriptId);
    @Delete("DELETE FROM role WHERE script_id = #{scriptId}") void deleteRolesByScriptId(Integer scriptId);
    @Delete("DELETE FROM role_act_content WHERE script_id = #{scriptId}") void deleteRoleActContentsByScriptId(Integer scriptId);
    @Delete("DELETE FROM script_clue WHERE script_id = #{scriptId}") void deleteCluesByScriptId(Integer scriptId);

    // === 游戏运行相关 ===
    @Insert("INSERT INTO game_progress (user_id, script_id, user_role_id, current_act_id, phase, status) VALUES (#{userId}, #{scriptId}, #{userRoleId}, #{currentActId}, #{phase}, 'playing')")
    @Options(useGeneratedKeys = true, keyProperty = "gameId")
    void insertGameProgress(GameProgress progress);

    @Select("SELECT * FROM game_progress WHERE game_id = #{gameId}")
    GameProgress getGameProgress(Integer gameId);

    // 修改：将 voted_role_id 也更新进数据库
    @Update("UPDATE game_progress SET current_act_id = #{currentActId}, status = #{status}, phase = #{phase}, voted_role_id = #{votedRoleId} WHERE game_id = #{gameId}")
    void updateGameProgress(GameProgress progress);

    @Delete("DELETE FROM game_progress WHERE game_id = #{gameId}")
    void deleteGameProgress(Integer gameId);

    @Select("SELECT rac.* FROM role_act_content rac JOIN role r ON rac.role_id = r.role_id WHERE rac.act_id = #{actId} AND r.is_ai = 1")
    List<RoleActContent> getAiRoleContentsByAct(Integer actId);

    @Select("SELECT * FROM role WHERE role_id = #{roleId}")
    Role getRoleById(Integer roleId);

    // 恢复线索相关
    @Select("SELECT * FROM script_clue WHERE script_id = #{scriptId} AND unlock_chapter_id = #{actId}")
    List<ScriptClue> getCluesByAct(@Param("scriptId") Integer scriptId, @Param("actId") Integer actId);

    @Insert("INSERT IGNORE INTO game_unlocked_clue (game_id, clue_id) VALUES (#{gameId}, #{clueId})")
    void insertUnlockedClue(@Param("gameId") Integer gameId, @Param("clueId") Long clueId);

    @Select("SELECT c.* FROM script_clue c INNER JOIN game_unlocked_clue u ON c.clue_id = u.clue_id WHERE u.game_id = #{gameId}")
    List<ScriptClue> getUnlockedCluesByGame(Integer gameId);

    // 恢复聊天相关
    @Insert("INSERT INTO game_chat_record (game_id, act_id, sender_role_id, content) VALUES (#{gameId}, #{actId}, #{senderRoleId}, #{content})")
    void insertChatRecord(@Param("gameId") Integer gameId, @Param("actId") Integer actId, @Param("senderRoleId") Integer senderRoleId, @Param("content") String content);

    @Select("SELECT * FROM game_chat_record WHERE game_id = #{gameId} AND act_id = #{actId} ORDER BY send_time ASC")
    List<Map<String, Object>> getChatRecords(@Param("gameId") Integer gameId, @Param("actId") Integer actId);

    // ================== 新增：结局与复盘处理相关接口 ==================
    @Insert("INSERT INTO script_ending (script_id, voted_role_id, ending_title, ending_content) VALUES (#{scriptId}, #{votedRoleId}, #{endingTitle}, #{endingContent})")
    void insertScriptEnding(ScriptEnding ending);

    @Delete("DELETE FROM script_ending WHERE script_id = #{scriptId}")
    void deleteEndingsByScriptId(Integer scriptId);

    @Select("SELECT * FROM script_ending WHERE script_id = #{scriptId}")
    List<ScriptEnding> getEndingsByScriptId(Integer scriptId);

    @Select("SELECT * FROM script_ending WHERE script_id = #{scriptId} AND voted_role_id = #{votedRoleId} LIMIT 1")
    ScriptEnding getEndingByVote(@Param("scriptId") Integer scriptId, @Param("votedRoleId") Integer votedRoleId);

    @Select("SELECT truth_content FROM script WHERE script_id = #{scriptId}")
    String getScriptTruth(Integer scriptId);
}