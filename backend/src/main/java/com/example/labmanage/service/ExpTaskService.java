package com.example.labmanage.service;

import com.example.labmanage.entity.ExpTask;
import com.example.labmanage.repository.ClazzRepository;
import com.example.labmanage.repository.ExpTaskRepository;
import com.example.labmanage.repository.TermRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ExpTaskService {

    private static final Logger log = LoggerFactory.getLogger(ExpTaskService.class);
    private static final Pattern SHORT_TERM_PATTERN = Pattern.compile("^(\\d{4}-\\d{4})-(\\d+)$");

    @Autowired
    private ExpTaskRepository expTaskRepository;

    @Autowired
    private TermRepository termRepository;

    @Autowired
    private ClazzRepository clazzRepository;

    public List<ExpTask> listAll() {
        return expTaskRepository.findAll();
    }

    public ExpTask save(ExpTask expTask) {
        return expTaskRepository.save(expTask);
    }

    public Optional<ExpTask> getById(Integer id) {
        Optional<ExpTask> taskOpt = expTaskRepository.findById(id);
        if (taskOpt.isPresent()) {
            ExpTask task = taskOpt.get();
            resolveTermId(task);
            fillClassInfo(task);
        }
        return taskOpt;
    }

    private void resolveTermId(ExpTask task) {
        String term = task.getTerm();
        Integer taskId = task.getId();
        if (term == null || term.isBlank()) {
            log.warn("实验任务学期信息缺失：taskId={}", taskId);
            return;
        }

        String normalizedTerm = term.trim();
        String termName;
        if (isFullTermName(normalizedTerm)) {
            termName = normalizedTerm;
        } else {
            Matcher matcher = SHORT_TERM_PATTERN.matcher(normalizedTerm);
            if (!matcher.matches()) {
                log.warn("实验任务学期格式异常：taskId={}, term={}", taskId, term);
                return;
            }

            termName = buildTermName(matcher.group(1), matcher.group(2));
            if (termName == null) {
                log.warn("实验任务学期序号无法识别：taskId={}, term={}", taskId, term);
                return;
            }
        }

        termRepository.findByTermName(termName)
                .ifPresentOrElse(
                        matched -> task.setTermId(matched.getId()),
                        () -> log.warn("实验任务学期映射失败：taskId={}, term={}, expectedTermName={}", taskId, term, termName)
                );
    }

    private boolean isFullTermName(String term) {
        return term.contains("学年") && term.contains("学期");
    }

    private String buildTermName(String academicYear, String termNoText) {
        int termNo;
        try {
            termNo = Integer.parseInt(termNoText);
        } catch (NumberFormatException ex) {
            return null;
        }
        return switch (termNo) {
            case 1 -> academicYear + "学年第一学期";
            case 2 -> academicYear + "学年第二学期";
            case 3 -> academicYear + "学年第三学期";
            default -> null;
        };
    }

    private void fillClassInfo(ExpTask task) {
        if (task.getClassId() != null) {
            clazzRepository.findById(task.getClassId().longValue()).ifPresent(clazz -> {
                task.setClassName(clazz.getClazzName());
                task.setGrade(clazz.getGrade());
            });
        }
    }

    public void delete(Integer id) {
        expTaskRepository.deleteById(id);
    }
}
