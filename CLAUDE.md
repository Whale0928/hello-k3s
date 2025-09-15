# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 프로젝트 개요

Spring Boot 백엔드 개발자가 Kubernetes를 학습하고 실무에 적용하기 위한 문서화 프로젝트입니다.
- 현재: GitHub Actions를 이용한 다중 리눅스 서버 롤링 배포 운영 중
- 목표: K3s를 활용한 컨테이너 오케스트레이션 도입
- 환경: 미니PC(N100, 16GB RAM)를 활용한 홈랩 구축

## 프로젝트 구조

```
hello-k3s/
├── o1_쿠버네티스_아키텍처_이해하기.md     # Control Plane과 Worker Node 구조
├── o2_쿠버네티스_핵심_용어_정리.md       # Pod, Deployment, Service 등 용어 정리
├── o3_k3s와_k8s_비교_분석.md            # K3s vs K8s 차이점 및 선택 이유
├── o4_쿠버네티스_매니페스트_이해하기.md   # YAML 매니페스트 작성법
├── o5_마스터_노드_이해하기.md           # Control Plane 컴포넌트 상세
├── o6_워커_노드_이해하기.md             # Worker Node 컴포넌트 상세
└── o7_k3s_설치_가이드_및_트러블슈팅.md   # K3s v1.33.4 설치 및 트러블슈팅
```

## K3s 설치 및 관리 명령어

### 기본 설치
```bash
# K3s 단일 노드 설치 (최신 안정화 버전)
curl -sfL https://get.k3s.io | sh -

# 특정 버전 설치 (예: v1.33.4)
curl -sfL https://get.k3s.io | INSTALL_K3S_VERSION=v1.33.4+k3s1 sh -

# K3s 상태 확인
sudo systemctl status k3s
sudo k3s kubectl get nodes
```

### 클러스터 관리
```bash
# kubeconfig 설정
export KUBECONFIG=/etc/rancher/k3s/k3s.yaml
sudo chmod 644 /etc/rancher/k3s/k3s.yaml

# 노드 정보 확인
kubectl get nodes -o wide
kubectl describe node <node-name>

# 파드 관리
kubectl get pods --all-namespaces
kubectl logs <pod-name> -n <namespace>
kubectl exec -it <pod-name> -n <namespace> -- /bin/bash
```

### 트러블슈팅
```bash
# K3s 로그 확인
sudo journalctl -u k3s -f
sudo journalctl -u k3s --since "1 hour ago"

# K3s 재시작
sudo systemctl restart k3s

# K3s 완전 제거
/usr/local/bin/k3s-uninstall.sh
```

## 기술 스택 정보

**현재 운영 스택:**
- Backend: Spring Boot (Java 17)
- Cache: Redis
- Database: MySQL
- Frontend: Next.js

**목표 인프라:**
- Container Orchestration: K3s
- Container Runtime: containerd
- Network: Flannel
- Storage: Local Path Provisioner

## 하드웨어 환경

**미니PC 스펙:**
- CPU: Intel N100 (4 cores)
- RAM: 16GB DDR4
- Storage: 512GB NVMe SSD
- OS: Ubuntu 22.04 LTS (예정)

## 주요 참고사항

1. **K3s 선택 이유**: 경량화된 Kubernetes 배포판으로 미니PC 환경에 최적
   - 메모리 사용량: K3s(~1GB) vs K8s(5-10GB)
   - 단일 바이너리로 설치 간편
   - 완전한 Kubernetes API 호환성

2. **시스템 요구사항**:
   - 최소: CPU 1 core, RAM 512MB, Disk 200MB
   - 권장: CPU 2+ cores, RAM 1GB+, Disk 1GB+
   - 미니PC N100 16GB는 충분한 스펙

3. **네트워크 포트**:
   - 6443/tcp: K3s API Server
   - 10250/tcp: Kubelet metrics
   - 30000-32767/tcp: NodePort 서비스
   - 8472/udp: Flannel VXLAN

4. **설치 전 준비사항**:
   - Swap 비활성화 권장
   - 방화벽 규칙 설정 필요
   - 커널 모듈 활성화 확인