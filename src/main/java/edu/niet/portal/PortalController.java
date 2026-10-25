package edu.niet.portal;

import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class PortalController {
 private final JdbcTemplate db;
 public PortalController(JdbcTemplate db) { this.db = db; }

 @GetMapping("/students")
 public List<Map<String,Object>> students() {
  return db.queryForList("SELECT id,full_name AS fullName,roll_number AS rollNumber,department,year_of_study AS yearOfStudy FROM students ORDER BY full_name");
 }

 @GetMapping("/achievements")
 public List<Map<String,Object>> achievements(@RequestParam(required=false) String status,
                                               @RequestParam(required=false) String search) {
  StringBuilder sql = new StringBuilder("SELECT a.id,a.student_id AS studentId,s.full_name AS studentName,s.roll_number AS rollNumber,s.department,a.title,a.category,a.achievement_level AS achievementLevel,a.description,a.proof_url AS proofUrl,a.status,a.points,DATE_FORMAT(a.created_at,'%Y-%m-%d') AS createdAt FROM achievements a JOIN students s ON s.id=a.student_id WHERE 1=1");
  java.util.List<Object> args = new java.util.ArrayList<>();
  if(status != null && !status.isBlank() && !status.equalsIgnoreCase("ALL")) { sql.append(" AND a.status=?"); args.add(status.toUpperCase()); }
  if(search != null && !search.isBlank()) { sql.append(" AND (a.title LIKE ? OR s.full_name LIKE ? OR a.category LIKE ?)"); String q="%"+search.trim()+"%"; args.add(q); args.add(q); args.add(q); }
  sql.append(" ORDER BY a.created_at DESC,a.id DESC");
  return db.queryForList(sql.toString(), args.toArray());
 }

 @PostMapping("/achievements")
 @ResponseStatus(HttpStatus.CREATED)
 public Map<String,Object> add(@RequestBody AchievementRequest r) {
  if(r.studentId()==null || r.title()==null || r.title().isBlank() || r.category()==null || r.category().isBlank())
   throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Student, title and category are required.");
  Integer exists=db.queryForObject("SELECT COUNT(*) FROM students WHERE id=?",Integer.class,r.studentId());
  if(exists==null || exists==0) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Student does not exist.");
  db.update("INSERT INTO achievements(student_id,title,category,achievement_level,description,proof_url,status,points) VALUES(?,?,?,?,?,?,'PENDING',0)",
   r.studentId(),r.title().trim(),r.category(),blankDefault(r.achievementLevel(),"College"),blankDefault(r.description(),""),blankDefault(r.proofUrl(),""));
  Long id=db.queryForObject("SELECT LAST_INSERT_ID()",Long.class);
  return Map.of("message","Achievement submitted for faculty review.","id",id==null?0:id);
 }

 @PatchMapping("/achievements/{id}/review")
 public Map<String,Object> review(@PathVariable long id,@RequestBody ReviewRequest r) {
  if(r.status()==null || !List.of("VERIFIED","REJECTED","PENDING").contains(r.status().toUpperCase()))
   throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Status must be VERIFIED, REJECTED or PENDING.");
  Integer exists=db.queryForObject("SELECT COUNT(*) FROM achievements WHERE id=?",Integer.class,id);
  if(exists==null || exists==0) throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Achievement not found.");
  String status=r.status().toUpperCase();
  int points=status.equals("VERIFIED")?calculatePoints(r.category(),r.achievementLevel()):0;
  db.update("UPDATE achievements SET status=?,points=? WHERE id=?",status,points,id);
  return Map.of("message","Review saved.","status",status,"points",points);
 }

 @GetMapping("/leaderboard")
 public List<Map<String,Object>> leaderboard(@RequestParam(defaultValue="100") int limit) {
  int safe=Math.max(1,Math.min(limit,100));
  return db.queryForList("SELECT s.id AS studentId,s.full_name AS fullName,s.roll_number AS rollNumber,s.department,COUNT(a.id) AS verifiedAchievements,COALESCE(SUM(a.points),0) AS score FROM students s LEFT JOIN achievements a ON a.student_id=s.id AND a.status='VERIFIED' GROUP BY s.id,s.full_name,s.roll_number,s.department ORDER BY score DESC,verifiedAchievements DESC,s.full_name ASC LIMIT ?",safe);
 }

 @GetMapping("/stats")
 public Map<String,Object> stats() {
  Integer students=db.queryForObject("SELECT COUNT(*) FROM students",Integer.class);
  Integer total=db.queryForObject("SELECT COUNT(*) FROM achievements",Integer.class);
  Integer verified=db.queryForObject("SELECT COUNT(*) FROM achievements WHERE status='VERIFIED'",Integer.class);
  Integer pending=db.queryForObject("SELECT COUNT(*) FROM achievements WHERE status='PENDING'",Integer.class);
  return Map.of("students",students==null?0:students,"totalAchievements",total==null?0:total,
   "verifiedAchievements",verified==null?0:verified,"pendingAchievements",pending==null?0:pending);
 }

 private int calculatePoints(String category,String level) {
  // Illustrative demo policy; replace with a policy approved by faculty.
  String c=category==null?"":category.toLowerCase();
  int base=switch(c) {
   case "hackathon","competition" -> 10;
   case "internship" -> 20;
   case "project" -> 15;
   case "coding","dsa" -> 10;
   case "certification" -> 5;
   case "academic","academics" -> 15;
   case "sports","leadership" -> 10;
   default -> 5;
  };
  String l=level==null?"":level.toLowerCase();
  int multiplier=l.contains("international")?4:l.contains("national")?3:l.contains("state")?2:1;
  return base*multiplier;
 }
 private String blankDefault(String s,String d) { return s==null||s.isBlank()?d:s.trim(); }
 public record AchievementRequest(Long studentId,String title,String category,String achievementLevel,String description,String proofUrl) {}
 public record ReviewRequest(String status,String category,String achievementLevel) {}
}