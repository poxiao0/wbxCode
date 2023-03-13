package com.wbx.proj.dao;

import com.wbx.proj.entity.Message;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface MessageMapper {

    // 查询当前会话的列表，针对每个会话显示最新的私信
    List<Message> selectConversations(int userId, int offset, int limit);

    // 查询当前用户的会话总数
    int selectConversationCount(int userId);

    // 查询当前私信的列表
    List<Message> selectLetters(String conversationId, int offset, int limit);

    // 查询当前会话的私信总数
    int selectLetterCount(String conversationId);

    // 查询未读的消息数
    int selectLetterUnreadCount(int userId, String conversationId);

    // 插入新消息
    int insertLetter(Message message);

    // 修改消息的状态
    int updateLetter(List<Integer> ids,  int status);

    // 查询某个主题的最新消息
    Message selectLatestNotice(int userId, String topic);

    // 查询某个主题包含的通知数量
    int selectNoticeCount(int userId, String topic);

    // 查询某个主题的未读消息数
    int selectNoticeUnreadCount(int userId, String topic);

    // 查询某个主题的消息列表
    List<Message> selectNotices(int userId, String topic, int offset, int limit);
}
