import React, { useState, useEffect, useRef } from 'react';
import { Play, AlertCircle, CheckCircle, Clock, XCircle, RefreshCw, Database, Activity } from 'lucide-react';

const API_BASE_URL = 'http://localhost:8080/api';
const WS_URL = 'ws://localhost:8080/ws';

const TaskOrchestrator = () => {
  const [tasks, setTasks] = useState([]);
  const [workers, setWorkers] = useState([]);
  const [logs, setLogs] = useState([]);
  const [stats, setStats] = useState({ pending: 0, running: 0, completed: 0, failed: 0 });
  const [connected, setConnected] = useState(false);
  const logsEndRef = useRef(null);
  const wsRef = useRef(null);
  const reconnectTimeoutRef = useRef(null);

  // WebSocket connection with native WebSocket API
  useEffect(() => {
    connectWebSocket();
    fetchInitialData();

    return () => {
      if (wsRef.current) {
        wsRef.current.close();
      }
      if (reconnectTimeoutRef.current) {
        clearTimeout(reconnectTimeoutRef.current);
      }
    };
  }, []);

  const connectWebSocket = () => {
    try {
      const ws = new WebSocket(WS_URL + '/websocket');
      
      ws.onopen = () => {
        console.log('WebSocket connected');
        setConnected(true);
        
        // Subscribe to topics
        ws.send(JSON.stringify({
          type: 'SUBSCRIBE',
          topics: [
            '/topic/task.created',
            '/topic/task.started',
            '/topic/task.progress',
            '/topic/task.completed',
            '/topic/task.failed',
            '/topic/worker.registered',
            '/topic/worker.updated',
            '/topic/worker.failed',
            '/topic/logs'
          ]
        }));
      };

      ws.onmessage = (event) => {
        try {
          const data = JSON.parse(event.data);
          handleWebSocketMessage(data);
        } catch (error) {
          console.error('Error parsing WebSocket message:', error);
        }
      };

      ws.onerror = (error) => {
        console.error('WebSocket error:', error);
        setConnected(false);
      };

      ws.onclose = () => {
        console.log('WebSocket disconnected');
        setConnected(false);
        
        // Attempt to reconnect after 3 seconds
        reconnectTimeoutRef.current = setTimeout(() => {
          console.log('Attempting to reconnect...');
          connectWebSocket();
        }, 3000);
      };

      wsRef.current = ws;
    } catch (error) {
      console.error('Failed to connect WebSocket:', error);
      setConnected(false);
      
      // Fallback to polling if WebSocket fails
      startPolling();
    }
  };

  const handleWebSocketMessage = (data) => {
    switch(data.type) {
      case 'task.created':
      case 'task.started':
      case 'task.progress':
      case 'task.completed':
      case 'task.failed':
        if (data.payload) {
          updateTask(data.payload);
          fetchStatistics();
        }
        break;
      case 'worker.registered':
      case 'worker.updated':
      case 'worker.failed':
        if (data.payload) {
          updateWorker(data.payload);
        }
        break;
      case 'logs':
        if (data.payload) {
          addLog(data.payload);
        }
        break;
      default:
        console.log('Unknown message type:', data.type);
    }
  };

  // Polling fallback when WebSocket is not available
  const startPolling = () => {
    const pollInterval = setInterval(async () => {
      if (connected) {
        clearInterval(pollInterval);
        return;
      }
      
      try {
        await fetchTasks();
        await fetchWorkers();
        await fetchStatistics();
        await fetchLogs();
      } catch (error) {
        console.error('Polling error:', error);
      }
    }, 2000);

    return () => clearInterval(pollInterval);
  };

  const fetchInitialData = async () => {
    try {
      await Promise.all([
        fetchTasks(),
        fetchWorkers(),
        fetchStatistics(),
        fetchLogs()
      ]);
    } catch (error) {
      console.error('Error fetching initial data:', error);
    }
  };

  const fetchTasks = async () => {
    try {
      const response = await fetch(`${API_BASE_URL}/tasks/recent`);
      if (!response.ok) throw new Error('Failed to fetch tasks');
      const data = await response.json();
      setTasks(data);
    } catch (error) {
      console.error('Error fetching tasks:', error);
    }
  };

  const fetchWorkers = async () => {
    try {
      const response = await fetch(`${API_BASE_URL}/workers`);
      if (!response.ok) throw new Error('Failed to fetch workers');
      const data = await response.json();
      setWorkers(data);
    } catch (error) {
      console.error('Error fetching workers:', error);
    }
  };

  const fetchStatistics = async () => {
    try {
      const response = await fetch(`${API_BASE_URL}/tasks/statistics`);
      if (!response.ok) throw new Error('Failed to fetch statistics');
      const data = await response.json();
      setStats(data);
    } catch (error) {
      console.error('Error fetching statistics:', error);
    }
  };

  const fetchLogs = async () => {
    try {
      const response = await fetch(`${API_BASE_URL}/logs`);
      if (!response.ok) throw new Error('Failed to fetch logs');
      const data = await response.json();
      setLogs(data.reverse());
    } catch (error) {
      console.error('Error fetching logs:', error);
    }
  };

  const updateTask = (updatedTask) => {
    setTasks(prev => {
      const exists = prev.some(t => t.taskId === updatedTask.taskId);
      if (exists) {
        return prev.map(t => t.taskId === updatedTask.taskId ? updatedTask : t);
      }
      return [updatedTask, ...prev];
    });
  };

  const updateWorker = (updatedWorker) => {
    setWorkers(prev => {
      const exists = prev.some(w => w.workerId === updatedWorker.workerId);
      if (exists) {
        return prev.map(w => w.workerId === updatedWorker.workerId ? updatedWorker : w);
      }
      return [...prev, updatedWorker];
    });
  };

  const addLog = (log) => {
    setLogs(prev => [...prev.slice(-99), log]);
  };

  const createTask = async () => {
    try {
      const response = await fetch(`${API_BASE_URL}/tasks`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          name: `Task ${Date.now()}`,
          payload: JSON.stringify({ data: 'sample payload' })
        })
      });
      
      if (!response.ok) throw new Error('Failed to create task');
      
      const task = await response.json();
      updateTask(task);
      
      // Refresh data after creating task
      setTimeout(() => {
        fetchTasks();
        fetchStatistics();
      }, 500);
    } catch (error) {
      console.error('Error creating task:', error);
      alert('Failed to create task. Make sure the backend is running on http://localhost:8080');
    }
  };

  useEffect(() => {
    logsEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [logs]);

  // Auto-refresh data when not connected via WebSocket
  useEffect(() => {
    if (!connected) {
      const interval = setInterval(() => {
        fetchTasks();
        fetchWorkers();
        fetchStatistics();
      }, 2000);
      
      return () => clearInterval(interval);
    }
  }, [connected]);

  const getStatusIcon = (status) => {
    switch(status) {
      case 'PENDING': return <Clock className="w-4 h-4 text-yellow-500" />;
      case 'RUNNING': return <RefreshCw className="w-4 h-4 text-blue-500 animate-spin" />;
      case 'COMPLETED': return <CheckCircle className="w-4 h-4 text-green-500" />;
      case 'FAILED': return <XCircle className="w-4 h-4 text-red-500" />;
      case 'RETRYING': return <RefreshCw className="w-4 h-4 text-orange-500" />;
      default: return null;
    }
  };

  const getLogColor = (type) => {
    switch(type) {
      case 'ERROR': return 'text-red-400';
      case 'WARNING': return 'text-yellow-400';
      case 'SUCCESS': return 'text-green-400';
      default: return 'text-gray-300';
    }
  };

  const getWorkerStatusColor = (status) => {
    switch(status) {
      case 'ACTIVE': return 'bg-green-500/20 text-green-400';
      case 'IDLE': return 'bg-blue-500/20 text-blue-400';
      case 'FAILED': return 'bg-red-500/20 text-red-400';
      case 'RECOVERING': return 'bg-orange-500/20 text-orange-400';
      default: return 'bg-gray-500/20 text-gray-400';
    }
  };

  const formatTimestamp = (timestamp) => {
    if (!timestamp) return '';
    return new Date(timestamp).toLocaleTimeString();
  };

  const getGanttBars = () => {
    const now = Date.now();
    const timeWindow = 30000; // 30 seconds window
    
    return tasks
      .filter(t => t.startTime)
      .map(task => {
        const start = new Date(task.startTime).getTime();
        const end = task.endTime ? new Date(task.endTime).getTime() : now;
        
        const startPercent = Math.max(0, ((start - (now - timeWindow)) / timeWindow) * 100);
        const endPercent = Math.min(100, ((end - (now - timeWindow)) / timeWindow) * 100);
        const width = endPercent - startPercent;
        
        return { ...task, start: startPercent, width: Math.max(width, 2) };
      })
      .filter(task => task.width > 0);
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-gray-900 via-blue-900 to-gray-900 text-white p-6">
      <div className="max-w-7xl mx-auto">
        <div className="mb-8 flex items-center justify-between">
          <div>
            <h1 className="text-4xl font-bold mb-2 bg-gradient-to-r from-blue-400 to-cyan-400 bg-clip-text text-transparent">
              Distributed Task Orchestrator
            </h1>
            <p className="text-gray-400">Spring Boot + RabbitMQ + Redis + MySQL</p>
          </div>
          <div className="flex items-center gap-3">
            <div className={`flex items-center gap-2 px-3 py-1 rounded-full text-sm ${
              connected ? 'bg-green-500/20 text-green-400' : 'bg-yellow-500/20 text-yellow-400'
            }`}>
              <div className={`w-2 h-2 rounded-full ${connected ? 'bg-green-500 animate-pulse' : 'bg-yellow-500 animate-pulse'}`} />
              {connected ? 'WebSocket Connected' : 'Polling Mode'}
            </div>
            <Database className="w-5 h-5 text-blue-400" />
            <Activity className="w-5 h-5 text-cyan-400" />
          </div>
        </div>

        {/* Stats Cards */}
        <div className="grid grid-cols-4 gap-4 mb-6">
          <div className="bg-gray-800/50 backdrop-blur rounded-lg p-4 border border-yellow-500/30">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-yellow-400 text-sm font-medium">Pending</p>
                <p className="text-3xl font-bold">{stats.pending || 0}</p>
              </div>
              <Clock className="w-8 h-8 text-yellow-500" />
            </div>
          </div>
          
          <div className="bg-gray-800/50 backdrop-blur rounded-lg p-4 border border-blue-500/30">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-blue-400 text-sm font-medium">Running</p>
                <p className="text-3xl font-bold">{stats.running || 0}</p>
              </div>
              <RefreshCw className="w-8 h-8 text-blue-500" />
            </div>
          </div>
          
          <div className="bg-gray-800/50 backdrop-blur rounded-lg p-4 border border-green-500/30">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-green-400 text-sm font-medium">Completed</p>
                <p className="text-3xl font-bold">{stats.completed || 0}</p>
              </div>
              <CheckCircle className="w-8 h-8 text-green-500" />
            </div>
          </div>
          
          <div className="bg-gray-800/50 backdrop-blur rounded-lg p-4 border border-red-500/30">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-red-400 text-sm font-medium">Failed</p>
                <p className="text-3xl font-bold">{stats.failed || 0}</p>
              </div>
              <XCircle className="w-8 h-8 text-red-500" />
            </div>
          </div>
        </div>

        {/* Control Panel */}
        <div className="bg-gray-800/50 backdrop-blur rounded-lg p-4 mb-6 border border-gray-700">
          <button
            onClick={createTask}
            className="bg-gradient-to-r from-blue-500 to-cyan-500 hover:from-blue-600 hover:to-cyan-600 px-6 py-2 rounded-lg font-medium transition-all transform hover:scale-105"
          >
            <Play className="w-4 h-4 inline mr-2" />
            Create New Task
          </button>
          <span className="ml-4 text-sm text-gray-400">
            Tasks are distributed via RabbitMQ with distributed locks (Redisson)
          </span>
        </div>

        <div className="grid grid-cols-2 gap-6 mb-6">
          {/* Worker Status */}
          <div className="bg-gray-800/50 backdrop-blur rounded-lg p-6 border border-gray-700">
            <h2 className="text-xl font-bold mb-4 flex items-center gap-2">
              <div className="w-3 h-3 bg-green-500 rounded-full animate-pulse"></div>
              Worker Nodes ({workers.length})
            </h2>
            <div className="space-y-3">
              {workers.length === 0 ? (
                <div className="text-center text-gray-500 py-4">
                  No workers registered yet. Create a task to start workers.
                </div>
              ) : (
                workers.map(worker => (
                  <div key={worker.workerId} className="bg-gray-900/50 rounded-lg p-3 border border-gray-600">
                    <div className="flex items-center justify-between mb-2">
                      <span className="font-medium">{worker.workerId}</span>
                      <span className={`px-2 py-1 rounded text-xs font-medium ${getWorkerStatusColor(worker.status)}`}>
                        {worker.status}
                      </span>
                    </div>
                    <div className="text-sm text-gray-400">
                      <div>Tasks Processed: {worker.tasksProcessed}</div>
                      <div>Current: {worker.currentTaskId || 'Idle'}</div>
                      <div className="text-xs text-gray-500">
                        Last heartbeat: {formatTimestamp(worker.lastHeartbeat)}
                      </div>
                    </div>
                  </div>
                ))
              )}
            </div>
          </div>

          {/* Logs */}
          <div className="bg-gray-800/50 backdrop-blur rounded-lg p-6 border border-gray-700">
            <h2 className="text-xl font-bold mb-4">System Logs (MySQL)</h2>
            <div className="bg-gray-900/80 rounded-lg p-3 h-64 overflow-y-auto font-mono text-xs">
              {logs.length === 0 ? (
                <div className="text-gray-500 text-center py-8">No logs yet</div>
              ) : (
                logs.map((log, idx) => (
                  <div key={idx} className="mb-1">
                    <span className="text-gray-500">[{formatTimestamp(log.timestamp)}]</span>{' '}
                    <span className={getLogColor(log.type)}>{log.message}</span>
                    {log.taskId && <span className="text-gray-600 ml-2">({log.taskId})</span>}
                  </div>
                ))
              )}
              <div ref={logsEndRef} />
            </div>
          </div>
        </div>

        {/* Gantt Chart */}
        <div className="bg-gray-800/50 backdrop-blur rounded-lg p-6 border border-gray-700 mb-6">
          <h2 className="text-xl font-bold mb-4">Task Execution Timeline (Real-time)</h2>
          <div className="space-y-2">
            {getGanttBars().length === 0 ? (
              <div className="text-center text-gray-500 py-8">
                No active tasks in timeline. Create tasks to see them here!
              </div>
            ) : (
              getGanttBars().slice(-10).map((task) => (
                <div key={task.taskId} className="flex items-center gap-3">
                  <div className="w-32 text-sm truncate" title={task.name}>{task.name}</div>
                  <div className="flex-1 bg-gray-900/50 rounded-full h-8 relative overflow-hidden">
                    <div
                      className={`absolute h-full rounded-full transition-all duration-300 ${
                        task.status === 'COMPLETED' ? 'bg-green-500' : 
                        task.status === 'FAILED' ? 'bg-red-500' : 'bg-blue-500'
                      }`}
                      style={{ 
                        left: `${task.start}%`, 
                        width: `${task.width}%`,
                      }}
                    >
                      <div className="flex items-center justify-center h-full text-xs font-medium px-2">
                        {task.workerId || 'Processing...'}
                      </div>
                    </div>
                  </div>
                  {getStatusIcon(task.status)}
                </div>
              ))
            )}
          </div>
        </div>

        {/* Task List */}
        <div className="bg-gray-800/50 backdrop-blur rounded-lg p-6 border border-gray-700">
          <h2 className="text-xl font-bold mb-4">Task Queue (Recent 10)</h2>
          <div className="space-y-2">
            {tasks.length === 0 ? (
              <div className="text-center text-gray-500 py-8">
                No tasks yet. Click "Create New Task" to get started!
              </div>
            ) : (
              tasks.slice(0, 10).map(task => (
                <div key={task.taskId} className="bg-gray-900/50 rounded-lg p-3 border border-gray-600">
                  <div className="flex items-center justify-between mb-2">
                    <div className="flex items-center gap-3">
                      {getStatusIcon(task.status)}
                      <span className="font-medium">{task.name}</span>
                      <span className="text-xs text-gray-500">{task.taskId}</span>
                      {task.retryCount > 0 && (
                        <span className="text-xs px-2 py-1 bg-orange-500/20 text-orange-400 rounded">
                          Retry {task.retryCount}
                        </span>
                      )}
                    </div>
                    {task.workerId && (
                      <span className="text-xs text-gray-400">{task.workerId}</span>
                    )}
                  </div>
                  {task.status === 'RUNNING' && (
                    <div className="w-full bg-gray-700 rounded-full h-2 overflow-hidden">
                      <div 
                        className="bg-gradient-to-r from-blue-500 to-cyan-500 h-full transition-all duration-300"
                        style={{ width: `${task.progress || 0}%` }}
                      />
                    </div>
                  )}
                  {task.lockId && (
                    <div className="text-xs text-gray-500 mt-1">
                      🔒 Lock: {task.lockId.substring(0, 20)}...
                    </div>
                  )}
                  {task.errorMessage && (
                    <div className="text-xs text-red-400 mt-1">
                      ⚠️ {task.errorMessage}
                    </div>
                  )}
                </div>
              ))
            )}
          </div>
        </div>
      </div>
    </div>
  );
};

export default TaskOrchestrator;