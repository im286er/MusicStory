package com.cn;

import com.cn.dao.CommentRepository;
import com.cn.dao.CustomDAO;
import com.cn.dao.EssayRepository;
import com.cn.entity.Comment;
import com.cn.entity.Essay;
import com.cn.entity.News;
import com.cn.util.RestUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.ModelMap;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class EssayService {
    @Autowired
    EssayRepository essayRepository;
    @Autowired
    CommentRepository commentRepository;
    @Autowired
    CustomDAO customDAO;

    /**
     * 获取文章列表
     * @return
     */
    public ModelMap getEssayList(int page,int pageSize){
        String sql = "SELECT t.id,t.title,SUBSTRING(t.content,1,300)content,t3.username,t2.name,t.created_time,t.updated_time,t.read_num " +
                "FROM essay t,classify t2,`user` t3 WHERE t.classify_id=t2.id AND t.user_id=t3.id LIMIT ?,?";
        List<Map<String,Object>> essays = customDAO.nativeQueryListMap(sql,page-1,pageSize);
        return RestUtil.success(essays);
    }

    /**
     * 获取用户的文章列表
     * @param pageable 分页
     * @param userId 用户ID
     * @return
     */
    public ModelMap getUserEssayList(Pageable pageable,String userId){
        Page<Essay> essayList = essayRepository.findAll(pageable);
        return RestUtil.success(essayList.getContent());
    }

    /**
     * 根据ID获取文章详情
     * @param id 文章ID
     * @return
     */
    public Essay getEssayDetail(String id){
        return essayRepository.getById(id);
    }

    /**
     * 用户写文章
     * @param essay 文章内容
     */
    @Transactional
    public void createEssay(Essay essay){
        essayRepository.save(essay);
    }

    /**
     * 用户删除文章
     * @param userId  用户ID
     * @param essayId 文章ID
     */
    @Transactional
    public void delUserEssay(String userId,String essayId){
        essayRepository.deleteEssayByIdAndUserId(essayId,userId);
    }

    /**
     * 根据条件获取文章列表
     * @param pageable 分页
     * @param essay  条件
     */
    public Page<Essay> getEssayList(Pageable pageable, Essay essay){
        return essayRepository.findAll(EssayRepository.getEssayList(essay),pageable);
    }

    /**
     * 审查文章
     * @param essay
     */
    @Transactional
    public void altEssayState(Essay essay){
        Essay essay1 = essayRepository.getOne(essay.getId());
        essay1.setState(essay.getState());
        if(essay.getState()==2){//审核不通过
            essay1.setRemark(essay.getRemark());
            News news = new News();
            news.setUserId(essay1.getUser().getId());
            news.setContent(essay.getRemark());
            news.setCreateTime(new Date());
            news.setSenderId("1");
            news.setSendTime(new Date());
            //TODO 通知作者
        }
    }

    /**
     * 获取文章评论
     * @param id     文章Id
     * @param page   页数
     * @return
     */
    public List<Comment> getComments(String id,int page){
        Page<Comment> comments = commentRepository.findByEssayIdOrderByCreatedTimeDesc(id, PageRequest.of(page-1,20));
        List<Comment> commentList = comments.getContent();
        return commentList;
    }
}
