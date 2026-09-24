package com.example.scoring.controller;

import com.example.scoring.entity.*;
import com.example.scoring.repository.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDateTime;
import java.util.*;

@RestController @RequestMapping("/api") public class ScoringController {
 private final DepartmentRepository departments; private final UserRepository users; private final ScoreTemplateRepository templates;
 private final ScoreItemRepository items; private final ScoreTaskRepository tasks; private final TaskParticipantRepository participants;
 private final ScoreSubmissionRepository submissions; private final ScoreDetailRepository details; private final PasswordEncoder passwordEncoder;
 public ScoringController(DepartmentRepository d,UserRepository u,ScoreTemplateRepository t,ScoreItemRepository i,ScoreTaskRepository k,TaskParticipantRepository p,ScoreSubmissionRepository s,ScoreDetailRepository v, PasswordEncoder e){departments=d;users=u;templates=t;items=i;tasks=k;participants=p;submissions=s;details=v;passwordEncoder=e;}
 private static ResponseStatusException bad(String msg){return new ResponseStatusException(HttpStatus.BAD_REQUEST,msg);}
 public record DepartmentInput(@NotBlank String name,Long parentId){}
 @PostMapping("/departments") public Department createDepartment(@Valid @RequestBody DepartmentInput x){Department d=new Department();d.name=x.name();if(x.parentId()!=null)d.parent=departments.findById(x.parentId()).orElseThrow(()->bad("上级部门不存在"));return departments.save(d);}
 @GetMapping("/departments") public List<Department> departments(){return departments.findAll();}
 public record UserInput(@NotBlank String username,@NotBlank String name,@NotNull Long departmentId,@NotBlank String role,String password){}
 @PostMapping("/users") public User createUser(@Valid @RequestBody UserInput x){if(!Set.of("EMPLOYEE","LEADER").contains(x.role()))throw bad("role 只能为 EMPLOYEE 或 LEADER");User u=new User();u.username=x.username();u.name=x.name();u.role=x.role();u.password=passwordEncoder.encode(x.password()==null||x.password().isBlank()?"123456":x.password());u.department=departments.findById(x.departmentId()).orElseThrow(()->bad("部门不存在"));return users.save(u);}
 @GetMapping("/users") public List<User> users(){return users.findAll();}
 public record ItemInput(@NotBlank String title,@NotNull @Min(1) @Max(100) Integer maxScore){}
 public record TemplateInput(@NotBlank String name, EvaluationDirection direction, @NotEmpty List<@Valid ItemInput> items){}
 @Transactional @PostMapping("/templates") public ScoreTemplate createTemplate(@Valid @RequestBody TemplateInput x){if(x.items().size()!=10) throw bad("评分表必须正好包含10个问题"); int sum=x.items().stream().mapToInt(ItemInput::maxScore).sum(); if(sum!=100) throw bad("10个问题的最高分总和必须为100"); ScoreTemplate t=new ScoreTemplate();t.name=x.name();t.direction=x.direction()==null?EvaluationDirection.EMPLOYEE_TO_LEADER:x.direction();t.totalScore=100;t.questionCount=10;t.custom=true;templates.save(t);int order=1;for(ItemInput input:x.items()){ScoreItem i=new ScoreItem();i.title=input.title();i.maxScore=input.maxScore();i.sortOrder=order++;i.template=t;items.save(i);}return t;}
 @GetMapping("/templates") public List<ScoreTemplate> templates(){return templates.findAll();}
 @GetMapping("/templates/{id}/items") public List<ScoreItem> templateItems(@PathVariable Long id){return items.findByTemplateIdOrderBySortOrder(id);}
 public record TaskInput(@NotBlank String name,@NotNull Long leaderId,Long templateId,@NotEmpty List<@NotNull Long> departmentIds){}
 @Transactional @PostMapping("/tasks") public ScoreTask createTask(@Valid @RequestBody TaskInput x){User leader=users.findById(x.leaderId()).orElseThrow(()->bad("领导不存在"));if(!"LEADER".equals(leader.role))throw bad("被评价人必须是领导");ScoreTemplate template;
 if(x.templateId()==null){template=new ScoreTemplate();template.name=x.name()+" 默认评分表";template.custom=false;templates.save(template);String[] names={"工作能力","沟通协作","责任担当","廉洁自律"};for(int n=0;n<names.length;n++){ScoreItem i=new ScoreItem();i.template=template;i.title=names[n];i.sortOrder=n+1;i.maxScore=25;items.save(i);}}else{template=templates.findById(x.templateId()).orElseThrow(()->bad("评分表不存在"));if(items.findByTemplateIdOrderBySortOrder(template.id).isEmpty())throw bad("评分表没有项目");}
 ScoreTask task=new ScoreTask();task.name=x.name();task.targetLeader=leader;task.template=template;task.status="OPEN";tasks.save(task);
 Set<Long> ids=new HashSet<>(x.departmentIds());for(Long id:ids){if(!departments.existsById(id))throw bad("部门不存在: "+id);for(User u:users.findByDepartmentId(id)){if(!u.id.equals(leader.id)){TaskParticipant p=new TaskParticipant();p.task=task;p.evaluator=u;participants.save(p);}}}if(participants.findByTaskId(task.id).isEmpty())throw bad("没有可评分的员工");return task;}
 @GetMapping("/tasks") public List<ScoreTask> tasks(){return tasks.findAll();}
 @GetMapping("/tasks/{id}/participants") public List<TaskParticipant> taskParticipants(@PathVariable Long id){return participants.findByTaskId(id);}
 @PostMapping("/tasks/{id}/close") public ScoreTask close(@PathVariable Long id){ScoreTask t=tasks.findById(id).orElseThrow(()->bad("任务不存在"));t.status="CLOSED";return tasks.save(t);}
 public record SubmissionInput(@NotNull Long evaluatorId,@NotNull Map<Long,Integer> scores){}
 @Transactional @PostMapping("/tasks/{id}/submissions") public ScoreSubmission submit(@PathVariable Long id,@Valid @RequestBody SubmissionInput input){ScoreTask task=tasks.findById(id).orElseThrow(()->bad("任务不存在"));if(!"OPEN".equals(task.status))throw bad("任务已关闭");User evaluator=users.findById(input.evaluatorId()).orElseThrow(()->bad("员工不存在"));if(!participants.existsByTaskIdAndEvaluatorId(id,evaluator.id))throw bad("没有评分资格");if(submissions.existsByTaskIdAndEvaluatorId(id,evaluator.id))throw bad("不能重复提交");List<ScoreItem> required=items.findByTemplateIdOrderBySortOrder(task.template.id);Set<Long> requiredIds=new HashSet<>();for(ScoreItem item:required)requiredIds.add(item.id);if(!requiredIds.equals(input.scores().keySet()))throw bad("评分项目必须完整且不能包含其他项目");int total=0;for(ScoreItem item:required){Integer value=input.scores().get(item.id);if(value==null||value<0||value>item.maxScore)throw bad("项目分数超出范围: "+item.title);total+=value;}ScoreSubmission submission=new ScoreSubmission();submission.task=task;submission.evaluator=evaluator;submission.totalScore=total;submission.submittedAt=LocalDateTime.now();submissions.save(submission);for(ScoreItem item:required){ScoreDetail detail=new ScoreDetail();detail.submission=submission;detail.item=item;detail.score=input.scores().get(item.id);details.save(detail);}return submission;}
 public record Result(Long taskId,int participantCount,int submittedCount,double averageScore,List<Integer> totals){}
 @GetMapping("/tasks/{id}/results") public Result results(@PathVariable Long id){if(!tasks.existsById(id))throw bad("任务不存在");List<ScoreSubmission> rows=submissions.findByTaskId(id);List<Integer> scores=rows.stream().map(s->s.totalScore).toList();return new Result(id,participants.findByTaskId(id).size(),rows.size(),scores.stream().mapToInt(Integer::intValue).average().orElse(0),scores);}
}
