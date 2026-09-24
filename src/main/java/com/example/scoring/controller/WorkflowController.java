package com.example.scoring.controller;

import com.example.scoring.entity.EvaluationDirection;
import com.example.scoring.entity.ScoreDetail;
import com.example.scoring.entity.ScoreItem;
import com.example.scoring.entity.ScoreSubmission;
import com.example.scoring.entity.ScoreTask;
import com.example.scoring.entity.ScoreTemplate;
import com.example.scoring.entity.TaskParticipant;
import com.example.scoring.entity.User;
import com.example.scoring.repository.ScoreDetailRepository;
import com.example.scoring.repository.ScoreItemRepository;
import com.example.scoring.repository.ScoreSubmissionRepository;
import com.example.scoring.repository.ScoreTaskRepository;
import com.example.scoring.repository.ScoreTemplateRepository;
import com.example.scoring.repository.TaskParticipantRepository;
import com.example.scoring.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/workflow")
public class WorkflowController {

 private final ScoreTemplateRepository templates;
 private final ScoreItemRepository items;
 private final ScoreTaskRepository tasks;
 private final UserRepository users;
 private final TaskParticipantRepository participants;
 private final ScoreSubmissionRepository submissions;
 private final ScoreDetailRepository details;

 public WorkflowController(
         ScoreTemplateRepository templates,
         ScoreItemRepository items,
         ScoreTaskRepository tasks,
         UserRepository users,
         TaskParticipantRepository participants,
         ScoreSubmissionRepository submissions,
         ScoreDetailRepository details
 ) {
  this.templates = templates;
  this.items = items;
  this.tasks = tasks;
  this.users = users;
  this.participants = participants;
  this.submissions = submissions;
  this.details = details;
 }

 private ResponseStatusException bad(String message) {
  return new ResponseStatusException(
          HttpStatus.BAD_REQUEST,
          message
  );
 }

 /**
  * 查询评分表
  */
 @GetMapping("/templates")
 public List<ScoreTemplate> getTemplates() {
  return templates.findAll();
 }

 /**
  * 查询评分表及问题
  */
 @GetMapping("/templates/{id}")
 public Map<String, Object> getTemplate(
         @PathVariable Long id
 ) {
  ScoreTemplate template = templates.findById(id)
          .orElseThrow(() -> bad("评分表不存在"));

  List<ScoreItem> scoreItems =
          items.findByTemplateIdOrderBySortOrder(id);

  Map<String, Object> result = new HashMap<>();
  result.put("template", template);
  result.put("items", scoreItems);

  return result;
 }

 /**
  * 查询所有人员
  */
 @GetMapping("/users")
 public List<User> getUsers() {
  return users.findAll();
 }

 /**
  * 创建任务请求
  */
 public record TaskInput(
         String name,
         Long leaderId,
         Long templateId,
         List<Long> departmentIds
 ) {
 }

 /**
  * 创建评分任务
  */
 @Transactional
 @PostMapping("/tasks")
 public ScoreTask createTask(
         @RequestBody TaskInput input
 ) {
  if (input.name() == null || input.name().isBlank()) {
   throw bad("任务名称不能为空");
  }

  if (input.leaderId() == null) {
   throw bad("请选择被评价领导");
  }

  if (input.templateId() == null) {
   throw bad("请选择评分表");
  }

  User leader = users.findById(input.leaderId())
          .orElseThrow(() -> bad("被评价领导不存在"));

  if (!"LEADER".equals(leader.role)) {
   throw bad("被评价对象必须是领导");
  }

  ScoreTemplate template = templates.findById(input.templateId())
          .orElseThrow(() -> bad("评分表不存在"));

  List<ScoreItem> scoreItems =
          items.findByTemplateIdOrderBySortOrder(template.id);

  if (scoreItems.size() != 10) {
   throw bad("评分表必须包含10个问题");
  }

  ScoreTask task = new ScoreTask();
  task.name = input.name();
  task.status = "OPEN";
  task.targetLeader = leader;
  task.template = template;

  if (template.direction == null) {
   task.direction =
           EvaluationDirection.EMPLOYEE_TO_LEADER;
  } else {
   task.direction = template.direction;
  }

  tasks.save(task);

  int participantCount = 0;

  for (User user : users.findAll()) {

   if (user.id.equals(leader.id)) {
    continue;
   }

   if (input.departmentIds() != null
           && !input.departmentIds().isEmpty()
           && !input.departmentIds()
           .contains(user.department.id)) {
    continue;
   }

   TaskParticipant participant =
           new TaskParticipant();

   participant.task = task;
   participant.evaluator = user;

   participants.save(participant);
   participantCount++;
  }

  if (participantCount == 0) {
   throw bad("没有找到可以参与评分的员工");
  }

  return task;
 }

 /**
  * 查询任务
  */
 @GetMapping("/tasks")
 public List<ScoreTask> getTasks() {
  return tasks.findAll();
 }

 /**
  * 加载评分页面
  */
 @GetMapping("/tasks/{id}/form")
 public Map<String, Object> getScoreForm(
         @PathVariable Long id
 ) {
  ScoreTask task = tasks.findById(id)
          .orElseThrow(() -> bad("评分任务不存在"));

  List<ScoreItem> scoreItems =
          items.findByTemplateIdOrderBySortOrder(
                  task.template.id
          );

  List<TaskParticipant> taskParticipants =
          participants.findByTaskId(id);

  Map<String, Object> result = new HashMap<>();
  result.put("task", task);
  result.put("items", scoreItems);
  result.put("participants", taskParticipants);

  return result;
 }

 /**
  * 提交评分请求
  */
 public record SubmitInput(
         Long evaluatorId,
         Map<Long, Integer> scores
 ) {
 }

 /**
  * 提交评分
  */
 @Transactional
 @PostMapping("/tasks/{id}/submit")
 public ScoreSubmission submitScore(
         @PathVariable Long id,
         @RequestBody SubmitInput input
 ) {
  if (input.evaluatorId() == null) {
   throw bad("请选择评价人");
  }

  if (input.scores() == null) {
   throw bad("评分内容不能为空");
  }

  ScoreTask task = tasks.findById(id)
          .orElseThrow(() -> bad("评分任务不存在"));

  if (!"OPEN".equals(task.status)) {
   throw bad("评分任务已经关闭");
  }

  User evaluator = users.findById(input.evaluatorId())
          .orElseThrow(() -> bad("评价人不存在"));

  boolean hasPermission =
          participants.existsByTaskIdAndEvaluatorId(
                  id,
                  evaluator.id
          );

  if (!hasPermission) {
   throw new ResponseStatusException(
           HttpStatus.FORBIDDEN,
           "该人员没有评分资格"
   );
  }

  boolean submitted =
          submissions.existsByTaskIdAndEvaluatorId(
                  id,
                  evaluator.id
          );

  if (submitted) {
   throw bad("该人员已经提交过评分");
  }

  List<ScoreItem> scoreItems =
          items.findByTemplateIdOrderBySortOrder(
                  task.template.id
          );

  if (input.scores().size() != scoreItems.size()) {
   throw bad("必须完成全部评分项目");
  }

  int totalScore = 0;

  for (ScoreItem item : scoreItems) {

   Integer score = input.scores().get(item.id);

   if (score == null) {
    throw bad("存在未评分项目");
   }

   if (score < 1 || score > 10) {
    throw bad("每道题分数必须在1到10之间");
   }

   totalScore += score;
  }

  ScoreSubmission submission =
          new ScoreSubmission();

  submission.task = task;
  submission.evaluator = evaluator;
  submission.totalScore = totalScore;
  submission.submittedAt = LocalDateTime.now();

  submissions.save(submission);

  for (ScoreItem item : scoreItems) {

   ScoreDetail detail =
           new ScoreDetail();

   detail.submission = submission;
   detail.item = item;
   detail.score = input.scores().get(item.id);

   details.save(detail);
  }

  return submission;
 }

 /**
  * 领导查看评分结果
  */
 @GetMapping("/tasks/{id}/results")
 public List<ScoreSubmission> getResults(
         @PathVariable Long id
 ) {
  if (!tasks.existsById(id)) {
   throw bad("评分任务不存在");
  }

  return submissions.findByTaskId(id);
 }

 /**
  * 查看某一条评分的明细
  */
 @GetMapping("/submissions/{id}/details")
 public List<ScoreDetail> getDetails(
         @PathVariable Long id
 ) {
  return details.findBySubmissionId(id);
 }

 /**
  * 关闭评分任务
  */
 @PostMapping("/tasks/{id}/close")
 public ScoreTask closeTask(
         @PathVariable Long id
 ) {
  ScoreTask task = tasks.findById(id)
          .orElseThrow(() -> bad("评分任务不存在"));

  task.status = "CLOSED";

  return tasks.save(task);
 }
}